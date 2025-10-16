package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "english")
    val english: String,
    @ColumnInfo(name = "french")
    val french: String,
    @ColumnInfo(name = "theme")
    val theme: String,
    @ColumnInfo(name = "example")
    val example: String?,
    @ColumnInfo(name = "example_french")
    val exampleFrench: String?,
    @ColumnInfo(name = "srs_step")
    val srsStep: Int,
    @ColumnInfo(name = "next_review_epoch")
    val nextReviewEpochMillis: Long,
    @ColumnInfo(name = "last_review_epoch")
    val lastReviewEpochMillis: Long?,
    @ColumnInfo(name = "success_count")
    val successCount: Int,
    @ColumnInfo(name = "failure_count")
    val failureCount: Int
)
