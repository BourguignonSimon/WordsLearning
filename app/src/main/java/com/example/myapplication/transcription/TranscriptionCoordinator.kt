package com.example.myapplication.transcription

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class TranscriptionCoordinator(
    private val workManager: WorkManager
) {

    fun enqueueTranscription(sessionId: Long, useWhisper: Boolean = false) {
        val work = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(
                workDataOf(
                    TranscriptionWorker.KEY_SESSION_ID to sessionId,
                    TranscriptionWorker.KEY_USE_WHISPER to useWhisper
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            WORK_NAME_PREFIX + sessionId,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    companion object {
        private const val WORK_NAME_PREFIX = "transcription_work_"
    }
}
