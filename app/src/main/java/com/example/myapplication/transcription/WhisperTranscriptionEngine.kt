package com.example.myapplication.transcription

import com.example.myapplication.domain.model.TranscriptSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Placeholder Whisper JNI engine that mirrors the Vosk behaviour.
 * Replace with the actual JNI integration when available.
 */
class WhisperTranscriptionEngine : TranscriptionEngine {
    private val fallback = VoskTranscriptionEngine()

    override suspend fun transcribe(audioFile: File): List<TranscriptSegment> = withContext(Dispatchers.Default) {
        fallback.transcribe(audioFile)
    }
}
