package com.example.my_digital_lord

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.my_digital_lord.ui.theme.AppTheme
import com.example.my_digital_lord.ui.theme.IceBackground
import com.example.my_digital_lord.ui.theme.IcePrimary
import com.example.my_digital_lord.ui.theme.IceSecondary
import com.example.my_digital_lord.ui.theme.SunsetBackground
import com.example.my_digital_lord.ui.theme.SunsetPrimary
import com.example.my_digital_lord.ui.theme.SunsetSecondary
import com.example.my_digital_lord.ui.theme.TerminalBackground
import com.example.my_digital_lord.ui.theme.TerminalPrimary
import com.example.my_digital_lord.ui.theme.TerminalSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    tasksViewModel: TasksViewModel,
    timerViewModel: TimerViewModel
){
    val tasks by tasksViewModel.tasks.collectAsState()
    val productiveSeconds = timerViewModel.getProductiveTimeLast7Days()
    val topSessions = timerViewModel.getTopSessionsLast7Days()

    val completedTasks = tasks.filter { it.isCompleted }
    val productiveHours = productiveSeconds / 3600
    val productiveMinutes = (productiveSeconds % 3600) / 60

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "СТАТИСТИКА ОТ ГОСПОДИНА",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ПРОДУКТИВНОЕ ВРЕМЯ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$productiveHours ч $productiveMinutes мин",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (topSessions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "ТОП:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        topSessions.forEach { session ->
                            Text(
                                text = "• ${formatDuration(session.durationSeconds.toLong())}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ВЫПОЛНЕННЫЕ ЗАДАЧИ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${completedTasks.size}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 56.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Господин ${if (completedTasks.size > 5) "восхищен" else "ждёт большего"}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d ч %02d мин %02d сек", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    timerViewModel: TimerViewModel = viewModel(),
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    isGuestMode: Boolean
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val inactivityLimit by timerViewModel.inactivityLimitSeconds.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(100.dp)
            ) {
                if (userProfile?.avatar != null) {
                    AsyncImage(
                        model = userProfile!!.avatar,
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Господин следит за тобой",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Тёмная тема",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Тема приложения", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(AppTheme.entries) { theme ->
                            val isSelected = currentTheme == theme
                            val previewColors = when (theme) {
                                AppTheme.SUNSET_NEON -> listOf(SunsetPrimary, SunsetSecondary, SunsetBackground)
                                AppTheme.RETRO_TERMINAL -> listOf(TerminalPrimary, TerminalBackground, TerminalSurface)
                                AppTheme.ICE_GLITCH -> listOf(IcePrimary, IceSecondary, IceBackground)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onThemeChange(theme) }
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .size(60.dp, 40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(previewColors.first())
                                ) {
                                    previewColors.drop(1).forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))

            if (isGuestMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        authViewModel.logoutFromGuest()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Выйти из гостевого режима")
                }
            }
        }
    }
}

