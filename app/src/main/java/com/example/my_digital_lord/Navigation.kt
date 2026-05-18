package com.example.my_digital_lord

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val tasksViewModel: TasksViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                restoreState = true
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
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
                TasksScreen(viewModel = tasksViewModel, timerViewModel = timerViewModel)
            }
            composable(Screen.Stats.route) {
                StatsScreen(tasksViewModel = tasksViewModel, timerViewModel = timerViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onLogout = onLogout,
                    timerViewModel = timerViewModel
                )
            }
        }
    }
}