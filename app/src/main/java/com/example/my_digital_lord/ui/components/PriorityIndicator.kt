package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.Priority

@Composable
fun PriorityIndicator(
    priority: Priority,
    modifier: Modifier = Modifier.size(14.dp)
) {
    val color = when (priority) {
        Priority.LOW -> MaterialTheme.colorScheme.secondary
        Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        Priority.HIGH -> MaterialTheme.colorScheme.primary
        Priority.URGENT -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}