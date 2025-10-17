package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.data.local.RecordingSessionWithSegments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExportManager(
    private val context: Context,
    private val encryptionManager: EncryptionManager
) {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    suspend fun exportAsJson(session: RecordingSessionWithSegments): File = withContext(Dispatchers.IO) {
        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val destination = File(exportsDir, "session_${session.session.id}.json.enc")
        val tempFile = File.createTempFile("export_json_${session.session.id}", ".tmp", context.cacheDir)
        tempFile.writeText(session.session.summaryJson ?: "{}")
        encryptionManager.encryptFile(tempFile, destination)
        tempFile.delete()
        destination
    }

    suspend fun exportAsMarkdown(session: RecordingSessionWithSegments): File = withContext(Dispatchers.IO) {
        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val destination = File(exportsDir, "session_${session.session.id}.md.enc")
        val tempFile = File.createTempFile("export_md_${session.session.id}", ".tmp", context.cacheDir)
        val title = session.session.title ?: "Session"
        val builder = StringBuilder()
        builder.appendLine("# $title")
        builder.appendLine("- Début : ${formatter.format(session.session.startedAt)}")
        builder.appendLine("- Durée : ${session.session.durationMillis / 60000} minutes")
        builder.appendLine()
        builder.appendLine("## Résumé")
        builder.appendLine(session.session.summaryJson ?: "Non disponible")
        builder.appendLine()
        builder.appendLine("## Transcription")
        session.segments.sortedBy { it.index }.forEach { segment ->
            builder.appendLine("- [${segment.startMillis / 1000}s - ${segment.endMillis / 1000}s] ${segment.text}")
        }
        tempFile.writeText(builder.toString())
        encryptionManager.encryptFile(tempFile, destination)
        tempFile.delete()
        destination
    }
}
