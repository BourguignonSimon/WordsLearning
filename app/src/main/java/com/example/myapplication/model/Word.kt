package com.example.myapplication.model

data class Word(
    val id: Int,
    val english: String,
    val french: String,
    val theme: String,
    val example: String?,
    val exampleFrench: String?,
    val srsStep: Int,
    val nextReviewEpochMillis: Long,
    val lastReviewEpochMillis: Long?,
    val successCount: Int,
    val failureCount: Int
)
