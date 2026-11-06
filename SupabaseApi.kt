package com.medspace.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(val userMessage: String) : Exception(userMessage)
class SessionExpiredException : Exception("session expired")

class SupabaseApi(private val store: SessionStore) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val base = Config.SUPABASE_URL.trimEnd('/')

    // ---------- أدوات داخلية ----------

    private fun execute(request: Request): Pair<Int, String> {
        return try {
            client.newCall(request).execute().use { r -> r.code to (r.body?.string() ?: "") }
        } catch (e: IOException) {
            throw ApiException("تعذّر الاتصال بالإنترنت. تحقق من اتصالك وحاول مجددًا.")
        }
    }

    private fun anon(path: String): Request.Builder =
        Request.Builder().url("$base$path").header("apikey", Config.SUPABASE_ANON_KEY)

    private fun saveSession(o: JSONObject) {
        store.accessToken = o.getString("access_token")
        store.refreshToken = o.getString("refresh_token")
        store.userId = o.getJSONObject("user").getString("id")
    }

    private fun refreshSession() {
        val rt = store.refreshToken
        if (rt.isEmpty()) throw SessionExpiredException()
        val body = JSONObject().put("refresh_token", rt).toString().toRequestBody(jsonType)
        val (code, text) = execute(anon("/auth/v1/token?grant_type=refresh_token").post(body).build())
        when {
            code == 200 -> saveSession(JSONObject(text))
            code in 400..403 -> { store.clear(); throw SessionExpiredException() }
            else -> throw ApiException("تعذّر تحديث الجلسة (رمز $code).")
        }
    }

    private fun authed(path: String, method: String = "GET", json: String? = null): Pair<Int, String> {
        fun once(): Pair<Int, String> {
            val b = anon(path).header("Authorization", "Bearer ${store.accessToken}")
            if (json != null) b.header("Prefer", "return=minimal")
            b.method(method, json?.toRequestBody(jsonType))
            return execute(b.build())
        }
        var res = once()
        if (res.first == 401) {
            refreshSession()
            res = once()
        }
        return res
    }

    private fun login(email: String, password: String) {
        val body = JSONObject().put("email", email).put("password", password)
            .toString().toRequestBody(jsonType)
        val (code, text) = execute(anon("/auth/v1/token?grant_type=password").post(body).build())
        if (code != 200) {
            val b = text.lowercase()
            throw ApiException(
                when {
                    "email_not_confirmed" in b -> "يجب تأكيد البريد الإلكتروني أولًا."
                    "invalid" in b -> "البريد الإلكتروني أو كلمة المرور غير صحيحة."
                    else -> "تعذّر تسجيل الدخول (رمز $code). حاول مجددًا."
                }
            )
        }
        saveSession(JSONObject(text))
    }

    private fun parseProfile(o: JSONObject) = Profile(
        id = o.getString("id"),
        fullName = o.optString("full_name"),
        email = o.optString("email"),
        role = o.getString("role"),
        status = o.getString("status")
    )

    // ---------- واجهة عامة ----------

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        login(email.trim(), password)
    }

    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", JSONObject().put("full_name", name.trim()))
            .toString().toRequestBody(jsonType)
        val (code, text) = execute(anon("/auth/v1/signup").post(body).build())
        if (code !in 200..299) {
            val b = text.lowercase()
            throw ApiException(
                when {
                    "already" in b || "exists" in b -> "هذا البريد الإلكتروني مسجّل مسبقًا. جرّب تسجيل الدخول."
                    "weak" in b || "at least" in b || "password" in b -> "كلمة المرور ضعيفة. استخدم 6 أحرف على الأقل."
                    "email" in b -> "البريد الإلكتروني غير صالح."
                    else -> "تعذّر إنشاء الحساب (رمز $code). حاول مجددًا."
                }
            )
        }
        login(email.trim(), password)
    }

    suspend fun fetchProfile(): Profile = withContext(Dispatchers.IO) {
        val (code, text) = authed("/rest/v1/profiles?select=*&id=eq.${store.userId}")
        if (code != 200) throw ApiException("تعذّر تحميل بيانات حسابك (رمز $code).")
        val arr = JSONArray(text)
        if (arr.length() == 0) throw ApiException("لم يتم العثور على حسابك.")
        parseProfile(arr.getJSONObject(0))
    }

    suspend fun fetchProfiles(): List<Profile> = withContext(Dispatchers.IO) {
        val (code, text) = authed("/rest/v1/profiles?select=*&order=created_at.desc")
        if (code != 200) throw ApiException("تعذّر تحميل قائمة الطلاب (رمز $code).")
        val arr = JSONArray(text)
        List(arr.length()) { parseProfile(arr.getJSONObject(it)) }
    }

    suspend fun setStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        require(status in setOf("pending", "approved", "rejected", "suspended"))
        val json = JSONObject().put("status", status).toString()
        val (code, _) = authed("/rest/v1/profiles?id=eq.$id", "PATCH", json)
        if (code !in 200..299) throw ApiException("تعذّر تنفيذ العملية (رمز $code). قد لا تملك الصلاحية.")
    }

    fun signOutLocal() = store.clear()
}
