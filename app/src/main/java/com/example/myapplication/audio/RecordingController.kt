package com.example.myapplication.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.repository.RecordingRepository
import com.example.myapplication.domain.model.TranscriptionStatus
import com.example.myapplication.transcription.TranscriptionCoordinator
import com.example.myapplication.util.EncryptionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant

class RecordingController(
    private val context: Context,
    private val repository: RecordingRepository,
    private val encryptionManager: EncryptionManager,
    private val transcriptionCoordinator: TranscriptionCoordinator,
    private val scope: CoroutineScope,
    private val onError: (Throwable) -> Unit = {}
) {

    companion object {
        const val SAMPLE_RATE = 48_000
        const val CHANNEL_COUNT = 2
        private const val BITS_PER_SAMPLE = 16
    }

    private val audioDeviceSelector = AudioDeviceSelector()
    private val processingPipeline = AudioProcessingPipeline()
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var startInstant: Instant? = null
    private var startElapsed: Long = 0
    private lateinit var tempFile: File
    private lateinit var outputFile: File
    private var sessionId: Long = -1

    fun isRecording(): Boolean = recordJob?.isActive == true

    fun startRecording(title: String?): Long {
        if (isRecording()) {
            return sessionId
        }
        val audioManager = context.getSystemService<AudioManager>()
            ?: throw IllegalStateException("AudioManager not available")
        val preferredDevice = audioDeviceSelector.selectPreferredInput(audioManager)
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw IllegalStateException("Unable to determine buffer size")
        }
        val bufferSize = (minBufferSize * 2).coerceAtLeast(SAMPLE_RATE)
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .build()
        preferredDevice?.let { recorder.preferredDevice = it }

        audioRecord = recorder
        startInstant = Instant.now()
        startElapsed = SystemClock.elapsedRealtime()
        tempFile = File(context.cacheDir, "active_recording.wav").apply { parentFile?.mkdirs() }
        outputFile = File(context.filesDir, "audio/${startInstant!!.toEpochMilli()}.wav.enc")
        val writer = WavFileWriter(tempFile, SAMPLE_RATE, CHANNEL_COUNT, BITS_PER_SAMPLE)
        writer.open()

        recorder.startRecording()
        if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            recorder.release()
            throw IllegalStateException("Recorder failed to start")
        }

        val createdSessionJob = scope.launch(Dispatchers.IO) {
            sessionId = repository.createSession(
                title = title,
                startedAt = startInstant!!,
                durationMillis = 0,
                audioPath = outputFile.absolutePath,
                encrypted = true
            )
        }

        val job = scope.launch(Dispatchers.IO) {
            createdSessionJob.join()
            val activeSessionId = sessionId
            if (activeSessionId <= 0) {
                recorder.stop()
                recorder.release()
                writer.close()
                return@launch
            }
            val buffer = ShortArray(bufferSize / 2)
            val byteBuffer = ByteBuffer.allocate(buffer.size * 2)
            try {
                while (isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read <= 0) {
                        handleReadError(read)
                    }
                    val processed = processingPipeline.process(buffer, read)
                    byteBuffer.clear()
                    for (i in 0 until read) {
                        byteBuffer.putShort(processed[i])
                    }
                    writer.write(byteBuffer.array(), read * 2)
                }
            } catch (security: SecurityException) {
                repository.updateStatus(activeSessionId, TranscriptionStatus.FAILED)
                onError(security)
                throw security
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                repository.updateStatus(activeSessionId, TranscriptionStatus.FAILED)
                onError(throwable)
                throw throwable
            } finally {
                recorder.stop()
                recorder.release()
                writer.close()
                withContext(Dispatchers.IO) {
                    encryptionManager.encryptFile(tempFile, outputFile)
                    tempFile.delete()
                }
                val duration = SystemClock.elapsedRealtime() - startElapsed
                repository.updateDurationAndPath(activeSessionId, duration, outputFile.absolutePath)
                transcriptionCoordinator.enqueueTranscription(activeSessionId, BuildConfig.USE_WHISPER)
            }
        }

        recordJob = job
        job.invokeOnCompletion {
            audioRecord = null
            recordJob = null
            sessionId = -1
        }
        return sessionId
    }

    private fun handleReadError(code: Int) {
        when (code) {
            AudioRecord.ERROR_INVALID_OPERATION,
            AudioRecord.ERROR_BAD_VALUE,
            AudioRecord.ERROR_DEAD_OBJECT -> throw RecordingException("AudioRecord error: $code")
            0 -> throw RecordingException("No audio data captured")
            -1 -> throw RecordingException("AudioRecord returned unknown error")
        }
    }

    suspend fun stopRecording() {
        recordJob?.cancelAndJoin()
    }
}

class RecordingException(message: String) : Exception(message)
