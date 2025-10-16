package com.example.myapplication.data

import android.content.Context
import androidx.annotation.RawRes
import com.example.myapplication.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SeedWordLoader(
    private val context: Context,
    @RawRes private val dataResId: Int = R.raw.initial_words,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    fun loadSeedWords(): List<SeedWord> {
        val inputStream = context.resources.openRawResource(dataResId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val payload = json.decodeFromString(SeedWordsPayload.serializer(), jsonString)
        return payload.words.map { record ->
            SeedWord(
                english = record.english.trim(),
                french = record.french.trim(),
                theme = record.theme.trim(),
                example = record.example?.trim().takeUnless { it.isNullOrEmpty() },
                exampleFrench = record.exampleFrench?.trim().takeUnless { it.isNullOrEmpty() }
            )
        }
    }
}

@Serializable
private data class SeedWordsPayload(
    val words: List<SeedWordRecord>
)

@Serializable
private data class SeedWordRecord(
    val english: String,
    val french: String,
    val theme: String,
    val example: String? = null,
    @SerialName("example_french")
    val exampleFrench: String? = null
)
