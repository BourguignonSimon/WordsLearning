package com.example.myapplication.data.repository

import com.example.myapplication.data.local.RecordingSegmentEntity
import com.example.myapplication.data.local.RecordingSessionDao
import com.example.myapplication.data.local.RecordingSessionEntity
import com.example.myapplication.data.local.RecordingSessionWithSegments
import com.example.myapplication.domain.model.RecordingSession
import com.example.myapplication.domain.model.TranscriptionStatus
import com.example.myapplication.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RecordingRepository(private val sessionDao: RecordingSessionDao) {

    fun observeSessions(
        start: Instant?,
        end: Instant?,
        minDuration: Long?,
        maxDuration: Long?
    ): Flow<List<RecordingSession>> {
        return sessionDao.observeSessions(start, end, minDuration, maxDuration)
            .map { sessions -> sessions.map { it.toDomain() } }
    }

    fun searchSessions(query: String): Flow<List<RecordingSession>> {
        return sessionDao.searchSessions(query)
            .map { sessions -> sessions.map { it.toDomain() } }
    }

    suspend fun createSession(
        title: String?,
        startedAt: Instant,
        durationMillis: Long,
        audioPath: String,
        encrypted: Boolean,
        participants: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        topics: List<String> = emptyList()
    ): Long {
        val entity = RecordingSessionEntity(
            title = title,
            startedAt = startedAt,
            durationMillis = durationMillis,
            audioPath = audioPath,
            encrypted = encrypted,
            transcriptionStatus = TranscriptionStatus.PENDING,
            summaryJson = null,
            participants = participants.joinToString(","),
            tags = tags.joinToString(","),
            topics = topics.joinToString(","),
            lastUpdated = startedAt
        )
        val id = sessionDao.insertSession(entity)
        sessionDao.upsertFts(id, title, null, null, null, null)
        return id
    }

    suspend fun updateStatus(sessionId: Long, status: TranscriptionStatus) {
        sessionDao.updateStatus(sessionId, status, Instant.now())
    }

    suspend fun updateDurationAndPath(sessionId: Long, durationMillis: Long, audioPath: String) {
        sessionDao.updateDurationAndPath(sessionId, durationMillis, audioPath, Instant.now())
    }

    suspend fun replaceSegments(sessionId: Long, segments: List<TranscriptSegment>) {
        sessionDao.deleteSegments(sessionId)
        sessionDao.insertSegments(segments.map { it.toEntity(sessionId) })
    }

    suspend fun updateSummary(
        sessionId: Long,
        title: String?,
        status: TranscriptionStatus,
        summaryJson: String?,
        participants: List<String>,
        tags: List<String>,
        topics: List<String>
    ) {
        sessionDao.updateSummary(
            sessionId = sessionId,
            status = status,
            summaryJson = summaryJson,
            participants = participants.joinToString(","),
            tags = tags.joinToString(","),
            topics = topics.joinToString(","),
            updatedAt = Instant.now()
        )
        sessionDao.upsertFts(
            rowId = sessionId,
            title = title,
            summary = summaryJson,
            participants = participants.joinToString(","),
            tags = tags.joinToString(","),
            topics = topics.joinToString(",")
        )
    }

    suspend fun getSessionWithSegments(sessionId: Long): RecordingSessionWithSegments? {
        return sessionDao.getSessionWithSegments(sessionId)
    }
}

private fun RecordingSessionWithSegments.toDomain(): RecordingSession {
    return RecordingSession(
        id = session.id,
        title = session.title,
        startedAt = session.startedAt,
        durationMillis = session.durationMillis,
        audioPath = session.audioPath,
        encrypted = session.encrypted,
        transcriptionStatus = session.transcriptionStatus,
        summaryJson = session.summaryJson,
        participants = session.participants?.split(',')?.filter { it.isNotBlank() } ?: emptyList(),
        tags = session.tags?.split(',')?.filter { it.isNotBlank() } ?: emptyList(),
        topics = session.topics?.split(',')?.filter { it.isNotBlank() } ?: emptyList()
    )
}

private fun TranscriptSegment.toEntity(sessionId: Long): RecordingSegmentEntity {
    return RecordingSegmentEntity(
        sessionId = sessionId,
        index = index,
        startMillis = startMillis,
        endMillis = endMillis,
        speaker = speaker,
        text = text
    )
}
