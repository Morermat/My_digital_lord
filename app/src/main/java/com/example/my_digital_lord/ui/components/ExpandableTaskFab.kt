package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableTaskFab(onAddTask: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onAddTask,
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.size(56.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Добавить")
    }
}