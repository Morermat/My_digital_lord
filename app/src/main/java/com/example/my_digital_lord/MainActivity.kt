package com.example.my_digital_lord

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_digital_lord.ui.theme.My_digital_lordTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("ScheduleExactAlarm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val timerViewModel: TimerViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> timerViewModel.onAppBackgrounded()
                        Lifecycle.Event.ON_RESUME -> timerViewModel.onAppForegrounded()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            AppRoot(
                timerViewModel = timerViewModel,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun AppRoot(
    timerViewModel: TimerViewModel,
    authViewModel: AuthViewModel
) {
    var screen by remember { mutableStateOf(AppScreen.Splash) }
    var isDarkTheme by remember { mutableStateOf(true) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        com.example.my_digital_lord.di.ServiceLocator.setSessionExpiredCallback {
            authViewModel.logout()
            screen = AppScreen.Auth
        }
    }

    My_digital_lordTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (screen) {
                AppScreen.Splash -> SplashScreen(
                    onFinished = {
                        screen = if (isLoggedIn) AppScreen.Main else AppScreen.Auth
                    }
                )
                AppScreen.Auth -> AuthScreen(
                    onLoginSuccess = { screen = AppScreen.Main },
                    authViewModel = authViewModel
                )
                AppScreen.Main -> Navigation(
                    timerViewModel = timerViewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    onLogout = {
                        authViewModel.logout()
                        screen = AppScreen.Auth
                    },
                    authViewModel = authViewModel
                )
            }
        }
    }
}

enum class AppScreen {
    Splash, Auth, Main
}