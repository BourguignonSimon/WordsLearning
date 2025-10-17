package com.example.myapplication.data

import android.content.Context
import androidx.work.WorkManager
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.preferences.DatabaseKeyProvider
import com.example.myapplication.data.repository.RecordingRepository
import com.example.myapplication.transcription.TranscriptionCoordinator
import com.example.myapplication.util.EncryptionManager
import com.example.myapplication.util.ModelImportManager
import com.example.myapplication.util.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val encryptionManagerDelegate by lazy { EncryptionManager(applicationContext) }
    private val databaseKeyProvider by lazy { DatabaseKeyProvider(applicationContext) }

    private val database: AppDatabase by lazy {
        AppDatabase.build(applicationContext, databaseKeyProvider.getOrCreatePassphrase())
    }

    val recordingRepository: RecordingRepository by lazy {
        RecordingRepository(database.recordingSessionDao())
    }

    val transcriptionCoordinator: TranscriptionCoordinator by lazy {
        TranscriptionCoordinator(
            workManager = WorkManager.getInstance(applicationContext)
        )
    }

    val encryptionManager: EncryptionManager
        get() = encryptionManagerDelegate

    val securityManager: SecurityManager by lazy { SecurityManager(applicationContext) }

    val modelImportManager: ModelImportManager by lazy { ModelImportManager(applicationContext) }
}
