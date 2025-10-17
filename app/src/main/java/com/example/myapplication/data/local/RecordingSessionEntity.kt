package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.TranscriptionStatus
import java.time.Instant

@Entity(tableName = "recording_sessions")
data class RecordingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String?,
    @ColumnInfo(name = "started_at")
    val startedAt: Instant,
    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long,
    @ColumnInfo(name = "audio_path")
    val audioPath: String,
    val encrypted: Boolean,
    @ColumnInfo(name = "transcription_status")
    val transcriptionStatus: TranscriptionStatus,
    @ColumnInfo(name = "summary_json")
    val summaryJson: String?,
    val participants: String?,
    val tags: String?,
    val topics: String?,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Instant
)
