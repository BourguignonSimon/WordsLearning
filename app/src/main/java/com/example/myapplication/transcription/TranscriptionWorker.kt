package com.example.myapplication.transcription

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.BuildConfig
import com.example.myapplication.WordsLearningApp
import com.example.myapplication.domain.model.TranscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getLong(KEY_SESSION_ID, -1)
        if (sessionId <= 0) return Result.failure()
        val useWhisper = inputData.getBoolean(KEY_USE_WHISPER, BuildConfig.USE_WHISPER)
        val app = applicationContext as WordsLearningApp
        val repository = app.container.recordingRepository
        val encryptionManager = app.container.encryptionManager
        val summaryGenerator = SummaryGenerator()
        val engine: TranscriptionEngine = if (useWhisper) WhisperTranscriptionEngine() else VoskTranscriptionEngine()
        return try {
            repository.updateStatus(sessionId, TranscriptionStatus.PROCESSING)
            val sessionWithSegments = repository.getSessionWithSegments(sessionId)
                ?: return Result.failure()
            val session = sessionWithSegments.session
            val encryptedFile = File(session.audioPath)
            if (!encryptedFile.exists()) {
                repository.updateStatus(sessionId, TranscriptionStatus.FAILED)
                return Result.failure()
            }
            val tempFile = File(applicationContext.cacheDir, "transcribe_$sessionId.wav")
            encryptionManager.decryptToTempFile(encryptedFile, tempFile)
            val segments = engine.transcribe(tempFile)
            repository.replaceSegments(sessionId, segments)
            val summaryJson = summaryGenerator.generateSummary(session.title, segments)
            repository.updateSummary(
                sessionId = sessionId,
                title = session.title,
                status = TranscriptionStatus.COMPLETED,
                summaryJson = summaryJson,
                participants = emptyList(),
                tags = emptyList(),
                topics = emptyList()
            )
            withContext(Dispatchers.IO) { tempFile.delete() }
            Result.success()
        } catch (throwable: Throwable) {
            repository.updateStatus(sessionId, TranscriptionStatus.FAILED)
            Result.retry()
        }
    }

    companion object {
        const val KEY_SESSION_ID = "key_session_id"
        const val KEY_USE_WHISPER = "key_use_whisper"
    }
}
