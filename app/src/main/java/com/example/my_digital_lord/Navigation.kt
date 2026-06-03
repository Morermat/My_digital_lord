package com.example.my_digital_lord

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.my_digital_lord.ui.screens.TasksScreen
import com.example.my_digital_lord.ui.theme.AppTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Tasks : Screen("tasks", "Задачи", Icons.AutoMirrored.Filled.List)
    data object Stats : Screen("stats", "Статистика", Icons.Default.Analytics)
    data object Profile : Screen("profile", "Профиль", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Tasks, Screen.Stats, Screen.Profile)

@Composable
fun Navigation(
    timerViewModel: TimerViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val navController = rememberNavController()
    val tasksViewModel: TasksViewModel = viewModel()
    var showTimerSheet by remember { mutableStateOf(false) }
    var bottomBarVisible by remember { mutableStateOf(true) }


    Scaffold(
        bottomBar = {
            if (bottomBarVisible) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Существующие три пункта навигации
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    restoreState = true
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                }
                            }
                        )
                    }

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Timer, contentDescription = "Таймер") },
                        label = { Text("Таймер") },
                        selected = false,
                        onClick = { showTimerSheet = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = tasksViewModel,
                    timerViewModel = timerViewModel,
                    onVoiceInput = {},
                    onTimer = {}
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(
                    tasksViewModel = tasksViewModel,
                    timerViewModel = timerViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onLogout = onLogout,
                    authViewModel = authViewModel,
                    timerViewModel = timerViewModel,
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    isGuestMode = authViewModel.isGuestMode.collectAsState().value
                )
            }
        }
    }

    if (showTimerSheet) {
        TimerBottomSheet(
            viewModel = timerViewModel,
            onDismiss = { showTimerSheet = false },
            onVisibilityChange = { visible -> bottomBarVisible = visible }
        )
        LaunchedEffect(showTimerSheet) {
            if (showTimerSheet) {
                bottomBarVisible = false
            }
        }
    }
}