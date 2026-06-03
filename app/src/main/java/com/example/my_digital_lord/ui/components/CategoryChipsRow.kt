package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.TaskCategory

@Composable
fun CategoryChipsRow(
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = TaskCategory.entries.toTypedArray()

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val label = when (category) {
                TaskCategory.ALL -> "Все"
                TaskCategory.WORK -> "Работа"
                TaskCategory.STUDY -> "Учёба"
                TaskCategory.PERSONAL -> "Личное"
                TaskCategory.HEALTH -> "Здоровье"
            }

            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors()  // без параметров
            )
        }
    }
}