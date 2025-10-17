package com.example.myapplication.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.WordsLearningApp
import com.example.myapplication.data.local.RecordingSessionWithSegments
import com.example.myapplication.data.repository.RecordingRepository
import com.example.myapplication.domain.model.RecordingSession
import com.example.myapplication.ui.state.SessionFilters
import com.example.myapplication.util.ExportManager
import com.example.myapplication.util.ModelImportManager
import com.example.myapplication.util.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as WordsLearningApp).container
    private val repository: RecordingRepository = container.recordingRepository
    private val exportManager = ExportManager(application, container.encryptionManager)

    val securityManager: SecurityManager = container.securityManager
    val modelImportManager: ModelImportManager = container.modelImportManager

    private val filters = MutableStateFlow(SessionFilters())
    private val searchQuery = MutableStateFlow("")
    private val _isRecording = MutableStateFlow(false)
    private val _lastExportResult = MutableStateFlow<File?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val isRecording: StateFlow<Boolean> = _isRecording
    val lastExportResult: StateFlow<File?> = _lastExportResult
    val errorMessage: StateFlow<String?> = _errorMessage
    val infoMessage: StateFlow<String?> = _infoMessage
    val currentFilters: StateFlow<SessionFilters> = filters
    val currentSearch: StateFlow<String> = searchQuery

    val sessions: StateFlow<List<RecordingSession>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                filters.flatMapLatest { filter ->
                    repository.observeSessions(
                        start = filter.startInstant,
                        end = filter.endInstant,
                        minDuration = filter.minDurationMillis,
                        maxDuration = filter.maxDurationMillis
                    )
                }
            } else {
                repository.searchSessions(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateFilters(newFilters: SessionFilters) {
        filters.value = newFilters
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun markRecording(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    fun clearExportResult() {
        _lastExportResult.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearInfo() {
        _infoMessage.value = null
    }

    fun enqueueTranscription(sessionId: Long) {
        container.transcriptionCoordinator.enqueueTranscription(sessionId, false)
    }

    fun exportSession(sessionId: Long, asMarkdown: Boolean) {
        viewModelScope.launch {
            try {
                val session = repository.getSessionWithSegments(sessionId)
                    ?: run {
                        _errorMessage.value = "Session introuvable"
                        return@launch
                    }
                val file = if (asMarkdown) {
                    exportManager.exportAsMarkdown(session)
                } else {
                    exportManager.exportAsJson(session)
                }
                _lastExportResult.value = file
            } catch (throwable: Throwable) {
                _errorMessage.value = throwable.message
            }
        }
    }

    fun importModel(uri: Uri, type: ModelImportManager.ModelType) {
        viewModelScope.launch {
            val result = modelImportManager.importModel(uri, type)
            if (result.success) {
                _infoMessage.value = result.message
            } else {
                _errorMessage.value = result.message
            }
        }
    }

    suspend fun loadSessionWithSegments(sessionId: Long): RecordingSessionWithSegments? {
        return repository.getSessionWithSegments(sessionId)
    }
}
