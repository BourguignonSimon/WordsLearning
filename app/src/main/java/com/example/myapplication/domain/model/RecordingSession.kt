package com.example.myapplication.domain.model

import java.time.Instant

/**
 * Represents a stored recording session with its metadata and summary.
 */
data class RecordingSession(
    val id: Long,
    val title: String?,
    val startedAt: Instant,
    val durationMillis: Long,
    val audioPath: String,
    val encrypted: Boolean,
    val transcriptionStatus: TranscriptionStatus,
    val summaryJson: String?,
    val participants: List<String>,
    val tags: List<String>,
    val topics: List<String>
)
