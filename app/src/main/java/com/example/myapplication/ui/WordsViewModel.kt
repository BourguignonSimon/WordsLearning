package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.WordsRepository
import com.example.myapplication.model.QuizQuestion
import com.example.myapplication.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WordsViewModel(
    private val repository: WordsRepository
) : ViewModel() {

    private val selectedThemes = MutableStateFlow<Set<String>>(emptySet())

    private val themesFlow = repository.observeThemes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val dueCountFlow = repository.observeDueCount(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val totalCountFlow = repository.observeTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val reviewUiState: StateFlow<ReviewUiState> = combine(
        themesFlow,
        selectedThemes,
        dueCountFlow,
        totalCountFlow
    ) { themes, selected, due, total ->
        ReviewUiState(
            themes = themes,
            selectedThemes = selected,
            dueCount = due,
            totalWords = total,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReviewUiState())

    val libraryUiState: StateFlow<LibraryUiState> = selectedThemes
        .flatMapLatest { repository.observeWordsByThemes(it) }
        .combine(selectedThemes) { words, selected ->
            LibraryUiState(
                words = words,
                selectedThemes = selected,
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    private val _quizState = MutableStateFlow(QuizUiState(isLoading = true))
    val quizUiState: StateFlow<QuizUiState> = _quizState

    fun toggleTheme(theme: String) {
        selectedThemes.update { current ->
            if (current.contains(theme)) current - theme else current + theme
        }
    }

    fun clearThemes() {
        selectedThemes.value = emptySet()
    }

    fun selectAllThemes() {
        selectedThemes.value = themesFlow.value.toSet()
    }

    fun startQuiz(optionCount: Int = DEFAULT_OPTION_COUNT) {
        viewModelScope.launch {
            _quizState.update { it.copy(isLoading = true, lastAnswerCorrect = null) }
            val question = repository.generateQuestion(selectedThemes.value, optionCount)
            _quizState.value = QuizUiState(
                currentQuestion = question,
                lastAnswerCorrect = null,
                questionsAnswered = 0,
                isLoading = false,
                optionCount = optionCount
            )
        }
    }

    fun loadNextQuestion() {
        val optionCount = _quizState.value.optionCount
        if (optionCount <= 0) return
        viewModelScope.launch {
            _quizState.update { it.copy(isLoading = true, lastAnswerCorrect = null) }
            val nextQuestion = repository.generateQuestion(selectedThemes.value, optionCount)
            _quizState.update { state ->
                state.copy(
                    currentQuestion = nextQuestion,
                    isLoading = false,
                    lastAnswerCorrect = null
                )
            }
        }
    }

    fun submitAnswer(optionId: Int) {
        val currentQuestion = _quizState.value.currentQuestion ?: return
        val selectedOption = currentQuestion.options.firstOrNull { it.id == optionId } ?: return
        val isCorrect = selectedOption.isCorrect
        viewModelScope.launch {
            repository.recordAnswer(currentQuestion.word, isCorrect)
            _quizState.update { state ->
                state.copy(
                    lastAnswerCorrect = isCorrect,
                    questionsAnswered = state.questionsAnswered + 1
                )
            }
        }
    }

    fun addWord(
        english: String,
        french: String,
        theme: String,
        example: String?,
        exampleFrench: String?
    ) {
        if (english.isBlank() || french.isBlank() || theme.isBlank()) {
            return
        }
        viewModelScope.launch {
            repository.addWord(english, french, theme, example, exampleFrench)
        }
    }

    data class ReviewUiState(
        val themes: List<String> = emptyList(),
        val selectedThemes: Set<String> = emptySet(),
        val dueCount: Int = 0,
        val totalWords: Int = 0,
        val isLoading: Boolean = true
    )

    data class LibraryUiState(
        val words: List<Word> = emptyList(),
        val selectedThemes: Set<String> = emptySet(),
        val isLoading: Boolean = true
    )

    data class QuizUiState(
        val currentQuestion: QuizQuestion? = null,
        val lastAnswerCorrect: Boolean? = null,
        val questionsAnswered: Int = 0,
        val isLoading: Boolean = true,
        val optionCount: Int = DEFAULT_OPTION_COUNT
    )

    companion object {
        const val DEFAULT_OPTION_COUNT = 5

        fun provideFactory(repository: WordsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(WordsViewModel::class.java)) {
                        return WordsViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
