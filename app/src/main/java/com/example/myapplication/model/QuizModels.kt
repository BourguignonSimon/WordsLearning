package com.example.myapplication.model

data class QuizOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

enum class QuizDirection {
    EN_TO_FR,
    FR_TO_EN
}

data class QuizQuestion(
    val word: Word,
    val prompt: String,
    val direction: QuizDirection,
    val options: List<QuizOption>
)
