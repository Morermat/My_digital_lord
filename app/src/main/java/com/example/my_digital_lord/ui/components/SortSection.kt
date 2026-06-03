package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SortOption {
    BY_DEADLINE, BY_PRIORITY, BY_STATUS, BY_CATEGORY
}

@Composable
fun SortSection(
    selectedSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Сортировка: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { expanded = true }) {
            Text(
                text = when (selectedSort) {
                    SortOption.BY_DEADLINE -> "По дате"
                    SortOption.BY_PRIORITY -> "По приоритету"
                    SortOption.BY_STATUS -> "По статусу"
                    SortOption.BY_CATEGORY -> "По категории"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (option) {
                                SortOption.BY_DEADLINE -> "По дате"
                                SortOption.BY_PRIORITY -> "По приоритету"
                                SortOption.BY_STATUS -> "По статусу"
                                SortOption.BY_CATEGORY -> "По категории"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}