package com.example.myapplication.ui.screens

import android.media.MediaPlayer
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.RecordingSegmentEntity
import com.example.myapplication.data.local.RecordingSessionWithSegments
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.util.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailScreen(
    sessionId: Long,
    viewModel: MainViewModel,
    encryptionManager: EncryptionManager,
    onExportMarkdown: () -> Unit,
    onExportJson: () -> Unit
) {
    val sessionState = produceState<RecordingSessionWithSegments?>(initialValue = null, sessionId) {
        value = viewModel.loadSessionWithSegments(sessionId)
    }
    val session = sessionState.value ?: return

    var playbackFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            playbackFile?.delete()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = session.session.title ?: "Session", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault()) }
        Text(text = "${formatter.format(session.session.startedAt)} - ${session.session.durationMillis / 60000} min")
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onExportMarkdown) { Text(text = stringResource(id = R.string.action_export_markdown)) }
            Button(onClick = onExportJson) { Text(text = stringResource(id = R.string.action_export_json)) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AudioPlayerControls(
            session = session,
            encryptionManager = encryptionManager,
            mediaPlayer = mediaPlayer,
            onMediaPlayerChange = { mediaPlayer = it },
            playbackFile = playbackFile,
            onPlaybackFileChange = { playbackFile = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCard(summaryJson = session.session.summaryJson)
        Spacer(modifier = Modifier.height(16.dp))
        TranscriptionList(segments = session.segments.sortedBy { it.index })
    }
}

@Composable
private fun AudioPlayerControls(
    session: RecordingSessionWithSegments,
    encryptionManager: EncryptionManager,
    mediaPlayer: MediaPlayer?,
    onMediaPlayerChange: (MediaPlayer?) -> Unit,
    playbackFile: File?,
    onPlaybackFileChange: (File?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = {
            scope.launch {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                } else {
                    val file = playbackFile ?: withContext(Dispatchers.IO) {
                        val target = File(context.cacheDir, "playback_${session.session.id}.wav")
                        encryptionManager.decryptToTempFile(File(session.session.audioPath), target)
                    }.also { onPlaybackFileChange(it) }
                    val player = mediaPlayer ?: MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                    }.also { onMediaPlayerChange(it) }
                    player.start()
                    isPlaying = true
                }
            }
        }) {
            Text(text = if (isPlaying) stringResource(id = R.string.player_pause) else stringResource(id = R.string.player_play))
        }
        TextButton(onClick = {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            onMediaPlayerChange(null)
            onPlaybackFileChange(null)
            isPlaying = false
        }) { Text(text = stringResource(id = R.string.player_stop)) }
    }
}

@Composable
private fun SummaryCard(summaryJson: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.detail_title_summary), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = summaryJson ?: stringResource(id = R.string.detail_empty_summary))
        }
    }
}

@Composable
private fun TranscriptionList(segments: List<RecordingSegmentEntity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.detail_title_transcription), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(240.dp)) {
                items(segments) { segment ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = "${segment.startMillis / 1000}s - ${segment.endMillis / 1000}s", fontWeight = FontWeight.Bold)
                        Text(text = segment.text)
                    }
                }
            }
        }
    }
}
