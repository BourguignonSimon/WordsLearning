package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(
    contentEntity = RecordingSessionEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"]
)
@Entity(tableName = "recording_sessions_fts")
data class RecordingSessionFtsEntity(
    val title: String?,
    val summaryJson: String?,
    val participants: String?,
    val tags: String?,
    val topics: String?
)
