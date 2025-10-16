package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ClearAll
import androidx.compose.material3.icons.filled.LibraryAddCheck
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.WordsViewModel

@Composable
fun ReviewScreen(
    state: WordsViewModel.ReviewUiState,
    optionCount: Int,
    onOptionCountChange: (Int) -> Unit,
    onThemeToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearThemes: () -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.review_dashboard_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        ReviewSummaryCard(state = state)
        Spacer(modifier = Modifier.height(24.dp))
        OptionCountSelector(optionCount = optionCount, onOptionCountChange = onOptionCountChange)
        Spacer(modifier = Modifier.height(24.dp))
        ThemeSelector(
            state = state,
            onThemeToggle = onThemeToggle,
            onSelectAll = onSelectAll,
            onClearThemes = onClearThemes
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStartQuiz,
            enabled = !state.isLoading && state.totalWords > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.action_start_quiz))
        }
    }
}

@Composable
private fun ReviewSummaryCard(state: WordsViewModel.ReviewUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.review_summary_due, state.dueCount),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.review_summary_total, state.totalWords),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun OptionCountSelector(
    optionCount: Int,
    onOptionCountChange: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.review_option_count_title, optionCount),
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = optionCount.toFloat(),
            onValueChange = { newValue ->
                onOptionCountChange(newValue.toInt().coerceIn(5, 10))
            },
            valueRange = 5f..10f,
            steps = 4
        )
    }
}

@Composable
private fun ThemeSelector(
    state: WordsViewModel.ReviewUiState,
    onThemeToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearThemes: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.review_theme_title),
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.LibraryAddCheck, contentDescription = null)
                }
                IconButton(onClick = onClearThemes) {
                    Icon(Icons.Default.ClearAll, contentDescription = null)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (state.themes.isEmpty()) {
            Text(text = stringResource(id = R.string.review_theme_empty))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 112.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(state.themes) { theme ->
                    FilterChip(
                        selected = state.selectedThemes.contains(theme),
                        onClick = { onThemeToggle(theme) },
                        label = { Text(theme) }
                    )
                }
            }
        }
    }
}
