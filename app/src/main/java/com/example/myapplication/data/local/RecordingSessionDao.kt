package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface RecordingSessionDao {

    @Transaction
    @Query(
        """
            SELECT * FROM recording_sessions
            WHERE (:start IS NULL OR started_at >= :start)
              AND (:end IS NULL OR started_at <= :end)
              AND (:minDuration IS NULL OR duration_millis >= :minDuration)
              AND (:maxDuration IS NULL OR duration_millis <= :maxDuration)
            ORDER BY started_at DESC
        """
    )
    fun observeSessions(
        start: Instant?,
        end: Instant?,
        minDuration: Long?,
        maxDuration: Long?
    ): Flow<List<RecordingSessionWithSegments>>

    @Transaction
    @Query(
        """
            SELECT rs.* FROM recording_sessions rs
            JOIN recording_sessions_fts fts ON rs.id = fts.rowid
            WHERE fts MATCH :query
            ORDER BY rs.started_at DESC
        """
    )
    fun searchSessions(query: String): Flow<List<RecordingSessionWithSegments>>

    @Transaction
    @Query("SELECT * FROM recording_sessions WHERE id = :sessionId")
    suspend fun getSessionWithSegments(sessionId: Long): RecordingSessionWithSegments?

    @Insert
    suspend fun insertSession(entity: RecordingSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<RecordingSegmentEntity>)

    @Query("DELETE FROM recording_segments WHERE session_id = :sessionId")
    suspend fun deleteSegments(sessionId: Long)

    @Query(
        "INSERT INTO recording_sessions_fts(rowid, title, summaryJson, participants, tags, topics) VALUES(:rowId, :title, :summary, :participants, :tags, :topics)"
    )
    suspend fun upsertFts(
        rowId: Long,
        title: String?,
        summary: String?,
        participants: String?,
        tags: String?,
        topics: String?
    )

    @Query("DELETE FROM recording_sessions_fts WHERE rowid = :rowId")
    suspend fun deleteFts(rowId: Long)

    @Query(
        "UPDATE recording_sessions SET transcription_status = :status, summary_json = :summaryJson, participants = :participants, tags = :tags, topics = :topics, last_updated = :updatedAt WHERE id = :sessionId"
    )
    suspend fun updateSummary(
        sessionId: Long,
        status: com.example.myapplication.domain.model.TranscriptionStatus,
        summaryJson: String?,
        participants: String?,
        tags: String?,
        topics: String?,
        updatedAt: Instant
    )

    @Query(
        "UPDATE recording_sessions SET transcription_status = :status, last_updated = :updatedAt WHERE id = :sessionId"
    )
    suspend fun updateStatus(
        sessionId: Long,
        status: com.example.myapplication.domain.model.TranscriptionStatus,
        updatedAt: Instant
    )

    @Query(
        "UPDATE recording_sessions SET duration_millis = :durationMillis, audio_path = :audioPath, last_updated = :updatedAt WHERE id = :sessionId"
    )
    suspend fun updateDurationAndPath(
        sessionId: Long,
        durationMillis: Long,
        audioPath: String,
        updatedAt: Instant
    )
}
