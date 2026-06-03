package com.example.my_digital_lord

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_digital_lord.di.ServiceLocator
import com.example.my_digital_lord.ui.theme.AppTheme
import com.example.my_digital_lord.ui.theme.MyDigitalLordTheme
import com.example.my_digital_lord.utils.ThemeManager
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    // Регистратор для разрешения на рисование поверх окон
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    // Регистратор для разрешения на уведомления
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение получено
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрос разрешения на рисование поверх окон (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }

        // Проверка точного будильника (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        // Запрос разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // После запросов других разрешений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1005)
            }
        }

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
            AppRoot(timerViewModel = timerViewModel, authViewModel = authViewModel)
        }
    }
}

@Composable
fun AppRoot(
    timerViewModel: TimerViewModel,
    authViewModel: AuthViewModel
) {
    var screen by remember { mutableStateOf(AppScreen.Splash) }
    val context = LocalContext.current
    var currentTheme by remember { mutableStateOf(ThemeManager.loadTheme(context)) }
    var isDarkTheme by remember { mutableStateOf(ThemeManager.loadDarkTheme(context)) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        ServiceLocator.setSessionExpiredCallback {
            authViewModel.logout()
            screen = AppScreen.Auth
        }
    }

    MyDigitalLordTheme(theme = currentTheme, darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (screen) {
                AppScreen.Splash -> SplashScreen(
                    onFinished = { screen = if (isLoggedIn) AppScreen.Main else AppScreen.Auth }
                )
                AppScreen.Auth -> AuthScreen(
                    onLoginSuccess = { screen = AppScreen.Main },
                    authViewModel = authViewModel
                )
                AppScreen.Main -> Navigation(
                    timerViewModel = timerViewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        ThemeManager.saveDarkTheme(context, isDarkTheme)
                    },
                    onLogout = {
                        authViewModel.logout()
                        screen = AppScreen.Auth
                    },
                    authViewModel = authViewModel,
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        ThemeManager.saveTheme(context, newTheme)
                    }
                )
            }
        }
    }
}

enum class AppScreen { Splash, Auth, Main }