package com.medspace.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medspace.app.data.AuthState
import com.medspace.app.data.AuthViewModel
import com.medspace.app.data.Profile
import com.medspace.app.ui.AboutScreen
import com.medspace.app.ui.AdminRequestsScreen
import com.medspace.app.ui.AuthScreen
import com.medspace.app.ui.BlockedScreen
import com.medspace.app.ui.ConfigMissingScreen
import com.medspace.app.ui.ErrorScreen
import com.medspace.app.ui.HomeScreen
import com.medspace.app.ui.MedSpaceTheme
import com.medspace.app.ui.SplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MedSpaceTheme { Root() } }
    }
}

@Composable
private fun Root() {
    val vm: AuthViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    var splashDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(1400); splashDone = true }

    if (!vm.configured) {
        ConfigMissingScreen()
        return
    }
    if (!splashDone) {
        SplashScreen()
        return
    }
    when (val s = state) {
        AuthState.Loading -> SplashScreen()
        AuthState.SignedOut -> AuthScreen(vm)
        is AuthState.Failure -> ErrorScreen(s.message, busy, onRetry = vm::loadProfile, onLogout = vm::signOut)
        is AuthState.Blocked -> BlockedScreen(s.profile, busy, onRefresh = vm::loadProfile, onLogout = vm::signOut)
        is AuthState.Active -> ActiveHost(s.profile, onLogout = vm::signOut)
    }
}

@Composable
private fun ActiveHost(profile: Profile, onLogout: () -> Unit) {
    var screen by rememberSaveable { mutableStateOf("home") }
    val target = if (screen == "admin" && !profile.isAdmin) "home" else screen
    BackHandler(enabled = target != "home") { screen = "home" }

    when (target) {
        "about" -> AboutScreen(onBack = { screen = "home" })
        "admin" -> AdminRequestsScreen(onBack = { screen = "home" }, onExpired = onLogout)
        else -> HomeScreen(
            profile = profile,
            onAdmin = { screen = "admin" },
            onAbout = { screen = "about" },
            onLogout = { screen = "home"; onLogout() }
        )
    }
}
