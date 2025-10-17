package com.example.myapplication.domain.model

/**
 * Represents a portion of a transcript along with its start/end timings in milliseconds.
 */
data class TranscriptSegment(
    val index: Int,
    val startMillis: Long,
    val endMillis: Long,
    val speaker: String?,
    val text: String
)
