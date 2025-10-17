package com.example.myapplication.transcription

import com.example.myapplication.domain.model.RecordingSummary
import com.example.myapplication.domain.model.TranscriptSegment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SummaryGenerator {

    private val json = Json { prettyPrint = true }

    fun generateSummary(sessionTitle: String?, segments: List<TranscriptSegment>): String {
        val textBody = segments.joinToString(" ") { it.text }
        val sentences = textBody.split('.').map { it.trim() }.filter { it.isNotBlank() }
        val summaryText = when {
            sentences.isEmpty() -> "Résumé automatique indisponible."
            sentences.size == 1 -> sentences.first()
            else -> sentences.take(3).joinToString(". ") + "."
        }
        val summary = RecordingSummary(
            title = sessionTitle ?: "Session audio",
            summary = summaryText,
            participants = emptyList(),
            actions = extractActionItems(sentences),
            decisions = emptyList(),
            sentiments = emptyMap(),
            tags = suggestTags(textBody),
            topics = suggestTopics(textBody),
            keywords = extractKeywords(textBody),
            timingSummaries = segments.take(5).map {
                RecordingSummary.SummaryTiming(
                    label = "Segment ${it.index + 1}",
                    startMillis = it.startMillis,
                    endMillis = it.endMillis
                )
            }
        )
        return json.encodeToString(summary)
    }

    private fun extractActionItems(sentences: List<String>): List<String> {
        return sentences.filter { sentence ->
            val lower = sentence.lowercase()
            lower.contains("doit") || lower.contains("action") || lower.contains("faire")
        }
    }

    private fun suggestTags(text: String): List<String> {
        val tags = mutableSetOf<String>()
        if (text.contains("projet", ignoreCase = true)) tags += "projet"
        if (text.contains("budget", ignoreCase = true)) tags += "budget"
        if (text.contains("client", ignoreCase = true)) tags += "client"
        if (text.contains("support", ignoreCase = true)) tags += "support"
        return tags.toList()
    }

    private fun suggestTopics(text: String): List<String> {
        return suggestTags(text)
    }

    private fun extractKeywords(text: String): List<String> {
        return text.split(' ', '.', ',', ';', ':')
            .map { it.trim() }
            .filter { it.length > 4 }
            .groupingBy { it.lowercase() }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }
}
