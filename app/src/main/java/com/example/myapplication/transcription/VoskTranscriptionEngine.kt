package com.example.myapplication.transcription

import com.example.myapplication.domain.model.TranscriptSegment
import com.example.myapplication.util.WavInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

/**
 * Placeholder Vosk engine that segments audio and returns synthetic transcripts.
 * This implementation is ready to be replaced by the real Vosk binding.
 */
class VoskTranscriptionEngine : TranscriptionEngine {

    override suspend fun transcribe(audioFile: File): List<TranscriptSegment> = withContext(Dispatchers.Default) {
        val metadata = WavInspector.readMetadata(audioFile)
        val windowSizeMillis = 30_000L
        val segments = mutableListOf<TranscriptSegment>()
        var index = 0
        var start = 0L
        while (start < metadata.durationMillis) {
            val end = min(start + windowSizeMillis, metadata.durationMillis)
            segments += TranscriptSegment(
                index = index,
                startMillis = start,
                endMillis = end,
                speaker = null,
                text = "Vosk placeholder segment ${index + 1} (${start / 1000}s-${end / 1000}s)"
            )
            start = end
            index++
        }
        if (segments.isEmpty()) {
            segments += TranscriptSegment(0, 0, metadata.durationMillis, null, "Vosk placeholder segment 1")
        }
        segments
    }
}
