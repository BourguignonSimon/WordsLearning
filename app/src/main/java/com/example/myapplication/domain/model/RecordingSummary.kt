package com.example.myapplication.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingSummary(
    val title: String,
    val summary: String,
    val participants: List<String> = emptyList(),
    val actions: List<String> = emptyList(),
    val decisions: List<String> = emptyList(),
    val sentiments: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    @SerialName("timings") val timingSummaries: List<SummaryTiming> = emptyList()
) {
    @Serializable
    data class SummaryTiming(
        val label: String,
        val startMillis: Long,
        val endMillis: Long
    )
}
