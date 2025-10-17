package com.example.myapplication.transcription

import com.example.myapplication.domain.model.TranscriptSegment
import java.io.File

interface TranscriptionEngine {
    suspend fun transcribe(audioFile: File): List<TranscriptSegment>
}
