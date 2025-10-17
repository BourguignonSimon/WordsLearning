package com.example.myapplication.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.WordsLearningApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecordService : Service() {

    private lateinit var controller: RecordingController
    private lateinit var serviceScope: CoroutineScope
    private var stopJob: Job? = null
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        val app = application as WordsLearningApp
        controller = RecordingController(
            context = this,
            repository = app.container.recordingRepository,
            encryptionManager = app.container.encryptionManager,
            transcriptionCoordinator = app.container.transcriptionCoordinator,
            scope = app.container.applicationScope,
            onError = { emitError(it) }
        )
        serviceScope = CoroutineScope(Dispatchers.Main + Job())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE)
                try {
                    controller.startRecording(title)
                    startForeground(NOTIFICATION_ID, buildNotification(title))
                    isForeground = true
                } catch (throwable: Throwable) {
                    emitError(throwable)
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopJob?.cancel()
                stopJob = serviceScope.launch {
                    controller.stopRecording()
                    if (isForeground) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        isForeground = false
                    }
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopJob?.cancel()
    }

    private fun buildNotification(title: String?): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title ?: getString(R.string.notification_recording_title))
            .setContentText(getString(R.string.notification_recording_description))
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun emitError(throwable: Throwable) {
        val intent = Intent(ACTION_RECORDING_ERROR).apply {
            putExtra(EXTRA_ERROR_MESSAGE, throwable.message)
        }
        sendBroadcast(intent)
        if (isForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForeground = false
        }
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_recording),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "recording_channel"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_TITLE = "extra_title"
        const val ACTION_RECORDING_ERROR = "com.example.myapplication.action.RECORDING_ERROR"
        private const val ACTION_START = "com.example.myapplication.action.START_RECORDING"
        private const val ACTION_STOP = "com.example.myapplication.action.STOP_RECORDING"
        const val EXTRA_ERROR_MESSAGE = "extra_error"

        fun start(context: Context, title: String?) {
            val intent = Intent(context, RecordService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RecordService::class.java).apply {
                action = ACTION_STOP
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
