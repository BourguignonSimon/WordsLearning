package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.myapplication.model.Word
import com.example.myapplication.ui.WordsViewModel

/**
 * Bibliothèque filtrable qui liste les mots en fonction des thèmes sélectionnés.
 */
@Composable
fun LibraryScreen(
    state: WordsViewModel.LibraryUiState,
    availableThemes: List<String>,
    onThemeToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.library_title),
                style = MaterialTheme.typography.headlineSmall
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
        Spacer(modifier = Modifier.height(12.dp))
        ThemeFilterRow(
            themes = availableThemes,
            selectedThemes = state.selectedThemes,
            onThemeToggle = onThemeToggle
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))
        WordList(words = state.words)
    }
}

/** Rangée de filtres thématiques affichée en haut de la bibliothèque. */
@Composable
private fun ThemeFilterRow(
    themes: List<String>,
    selectedThemes: Set<String>,
    onThemeToggle: (String) -> Unit
) {
    if (themes.isEmpty()) {
        Text(text = stringResource(id = R.string.library_empty_themes))
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(themes) { theme ->
                FilterChip(
                    selected = selectedThemes.contains(theme),
                    onClick = { onThemeToggle(theme) },
                    label = { Text(theme) }
                )
            }
        }
    }
}

/** Liste verticale des mots formatés pour une lecture rapide. */
@Composable
private fun WordList(words: List<Word>) {
    if (words.isEmpty()) {
        Text(text = stringResource(id = R.string.library_empty_words))
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(words) { word ->
            WordRow(word = word)
        }
    }
}

/** Présentation détaillée d'un mot (traduction + exemples). */
@Composable
private fun WordRow(word: Word) {
    Column {
        Text(
            text = "${word.english} → ${word.french}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(id = R.string.library_theme_label, word.theme),
            style = MaterialTheme.typography.labelSmall
        )
        word.example?.let { example ->
            Text(
                text = example,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        word.exampleFrench?.let { exampleFr ->
            Text(
                text = exampleFr,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
