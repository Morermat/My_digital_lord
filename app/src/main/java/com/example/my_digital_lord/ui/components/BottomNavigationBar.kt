package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Tasks : BottomNavScreen("tasks", "Задачи", Icons.AutoMirrored.Filled.List)
    object Stats : BottomNavScreen("stats", "Статистика", Icons.Default.Analytics)
    object Profile : BottomNavScreen("profile", "Профиль", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavScreen.Tasks,
            BottomNavScreen.Stats,
            BottomNavScreen.Profile
        )

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        screen.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}