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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.model.RecordingSession
import com.example.myapplication.domain.model.TranscriptionStatus
import com.example.myapplication.ui.state.SessionFilters
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    sessions: List<RecordingSession>,
    filters: SessionFilters,
    searchQuery: String,
    isRecording: Boolean,
    onSearchChange: (String) -> Unit,
    onFiltersChange: (SessionFilters) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onImportVosk: () -> Unit,
    onImportWhisper: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onRequestTranscription: (Long) -> Unit
) {
    var startDateText by remember(filters.startDate) { mutableStateOf(filters.startDate?.toString() ?: "") }
    var endDateText by remember(filters.endDate) { mutableStateOf(filters.endDate?.toString() ?: "") }
    var minDurationText by remember(filters.minDurationMinutes) { mutableStateOf(filters.minDurationMinutes?.toString() ?: "") }
    var maxDurationText by remember(filters.maxDurationMinutes) { mutableStateOf(filters.maxDurationMinutes?.toString() ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            actions = {
                TextButton(onClick = onImportVosk) {
                    Text(text = stringResource(id = R.string.action_import_vosk))
                }
                TextButton(onClick = onImportWhisper) {
                    Text(text = stringResource(id = R.string.action_import_whisper))
                }
                IconButton(onClick = { if (isRecording) onStopRecording() else onStartRecording() }) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text(stringResource(id = R.string.label_search)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = {
                        startDateText = it
                        onFiltersChange(filters.copy(startDate = it.toLocalDateOrNull()))
                    },
                    label = { Text(stringResource(id = R.string.label_start_date)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = {
                        endDateText = it
                        onFiltersChange(filters.copy(endDate = it.toLocalDateOrNull()))
                    },
                    label = { Text(stringResource(id = R.string.label_end_date)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minDurationText,
                    onValueChange = {
                        minDurationText = it
                        onFiltersChange(filters.copy(minDurationMinutes = it.toIntOrNull()))
                    },
                    label = { Text(stringResource(id = R.string.label_duration_min)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxDurationText,
                    onValueChange = {
                        maxDurationText = it
                        onFiltersChange(filters.copy(maxDurationMinutes = it.toIntOrNull()))
                    },
                    label = { Text(stringResource(id = R.string.label_duration_max)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(id = R.string.label_no_sessions))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sessions) { session ->
                    SessionRow(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        onRequestTranscription = { onRequestTranscription(session.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionRow(
    session: RecordingSession,
    onClick: () -> Unit,
    onRequestTranscription: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val started = remember(session.startedAt) { session.startedAt.atZone(ZoneId.systemDefault()).toLocalDateTime() }
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.title ?: "Session ${session.id}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatter.format(started), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = statusText(session.transcriptionStatus), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRequestTranscription) {
                    Text(text = stringResource(id = R.string.action_relaunch_transcription))
                }
                Text(text = "Durée : ${session.durationMillis / 60000} min")
            }
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

@Composable
private fun statusText(status: TranscriptionStatus): String {
    return when (status) {
        TranscriptionStatus.PENDING -> stringResource(id = R.string.label_transcription_pending)
        TranscriptionStatus.PROCESSING -> stringResource(id = R.string.label_transcription_processing)
        TranscriptionStatus.COMPLETED -> stringResource(id = R.string.label_transcription_completed)
        TranscriptionStatus.FAILED -> stringResource(id = R.string.label_transcription_failed)
    }
}
