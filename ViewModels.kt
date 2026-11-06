package com.medspace.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERIC_ERROR = "حدث خطأ غير متوقع. حاول مجددًا."

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SessionStore(app)
    private val api = SupabaseApi(store)

    val configured = Config.SUPABASE_URL.startsWith("https://") && Config.SUPABASE_ANON_KEY.length > 20

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (configured) {
            if (store.hasSession) loadProfile() else _state.value = AuthState.SignedOut
        }
    }

    fun clearError() { _error.value = null }

    private fun toState(p: Profile): AuthState =
        if (p.status == "approved") AuthState.Active(p) else AuthState.Blocked(p)

    private suspend fun refreshProfile() {
        _state.value = try {
            toState(api.fetchProfile())
        } catch (e: SessionExpiredException) {
            AuthState.SignedOut
        } catch (e: ApiException) {
            AuthState.Failure(e.userMessage)
        } catch (e: Exception) {
            AuthState.Failure(GENERIC_ERROR)
        }
    }

    fun loadProfile() {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try { refreshProfile() } finally { _busy.value = false }
        }
    }

    fun signIn(email: String, password: String) {
        if (_busy.value) return
        if (email.isBlank() || password.isEmpty()) {
            _error.value = "أدخل البريد الإلكتروني وكلمة المرور."
            return
        }
        runAuth { api.signIn(email, password) }
    }

    fun signUp(name: String, email: String, password: String, confirm: String) {
        if (_busy.value) return
        when {
            name.trim().length < 3 -> _error.value = "اكتب اسمك الكامل."
            !email.contains("@") || !email.contains(".") -> _error.value = "البريد الإلكتروني غير صالح."
            password.length < 6 -> _error.value = "كلمة المرور يجب ألا تقل عن 6 أحرف."
            password != confirm -> _error.value = "كلمتا المرور غير متطابقتين."
            else -> runAuth { api.signUp(name, email, password) }
        }
    }

    private fun runAuth(action: suspend () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            _error.value = null
            try {
                action()
                refreshProfile()
            } catch (e: ApiException) {
                _error.value = e.userMessage
            } catch (e: Exception) {
                _error.value = GENERIC_ERROR
            } finally {
                _busy.value = false
            }
        }
    }

    fun signOut() {
        api.signOutLocal()
        _error.value = null
        _state.value = AuthState.SignedOut
    }
}

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val api = SupabaseApi(SessionStore(app))

    private val _items = MutableStateFlow<List<Profile>>(emptyList())
    val items: StateFlow<List<Profile>> = _items.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    fun load() {
        if (_loading.value) return
        viewModelScope.launch { fetch() }
    }

    private suspend fun fetch() {
        _loading.value = true
        _error.value = null
        try {
            _items.value = api.fetchProfiles()
        } catch (e: SessionExpiredException) {
            _expired.value = true
        } catch (e: ApiException) {
            _error.value = e.userMessage
        } catch (e: Exception) {
            _error.value = GENERIC_ERROR
        } finally {
            _loading.value = false
        }
    }

    fun setStatus(id: String, status: String) {
        if (_loading.value) return
        viewModelScope.launch {
            _loading.value = true
            try {
                api.setStatus(id, status)
            } catch (e: SessionExpiredException) {
                _expired.value = true
            } catch (e: ApiException) {
                _error.value = e.userMessage
            } catch (e: Exception) {
                _error.value = GENERIC_ERROR
            }
            _loading.value = false
            fetch()
        }
    }
}
