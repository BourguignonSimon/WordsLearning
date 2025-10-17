package com.example.myapplication.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class RecordingSessionWithSegments(
    @Embedded val session: RecordingSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = RecordingSegmentEntity::class
    )
    val segments: List<RecordingSegmentEntity>
)
