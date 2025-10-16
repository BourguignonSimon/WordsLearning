package com.example.myapplication.data

import com.example.myapplication.data.local.WordDao
import com.example.myapplication.data.local.WordEntity
import com.example.myapplication.model.QuizDirection
import com.example.myapplication.model.QuizOption
import com.example.myapplication.model.QuizQuestion
import com.example.myapplication.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.min

class WordsRepository(
    private val wordDao: WordDao
) {

    private val srsIntervalsDays = listOf(1L, 3L, 7L, 30L)

    fun observeThemes(): Flow<List<String>> = wordDao.observeThemes()

    fun observeWordsByThemes(themes: Set<String>): Flow<List<Word>> {
        return if (themes.isEmpty()) {
            wordDao.observeAllWords().map { list -> list.map { it.toModel() } }
        } else {
            wordDao.observeWordsByThemes(themes.toList()).map { list -> list.map { it.toModel() } }
        }
    }

    fun observeDueCount(now: Long): Flow<Int> = wordDao.observeDueCount(now)

    fun observeTotalCount(): Flow<Int> = wordDao.observeTotalCount()

    suspend fun insertSeedWords(words: List<SeedWord>) {
        val now = System.currentTimeMillis()
        val entities = words.mapIndexed { index, seed ->
            WordEntity(
                id = index + 1,
                english = seed.english,
                french = seed.french,
                theme = seed.theme,
                example = seed.example,
                exampleFrench = seed.exampleFrench,
                srsStep = 0,
                nextReviewEpochMillis = now,
                lastReviewEpochMillis = null,
                successCount = 0,
                failureCount = 0
            )
        }
        wordDao.insertAll(entities)
    }

    suspend fun getDueWords(themes: Set<String>, limit: Int): List<Word> {
        val now = System.currentTimeMillis()
        val hasThemes = if (themes.isEmpty()) 0 else 1
        val due = wordDao.getDueWords(themes.toList(), now, limit, hasThemes)
        return due.map { it.toModel() }
    }

    suspend fun getUpcomingWords(themes: Set<String>, limit: Int): List<Word> {
        val hasThemes = if (themes.isEmpty()) 0 else 1
        return wordDao.getUpcomingWords(themes.toList(), limit, hasThemes).map { it.toModel() }
    }

    suspend fun generateQuestion(
        themes: Set<String>,
        optionCount: Int
    ): QuizQuestion? {
        val dueWord = getDueWords(themes, 1).firstOrNull()
            ?: getUpcomingWords(themes, 1).firstOrNull()
            ?: return null

        val direction = if ((dueWord.id + dueWord.srsStep) % 2 == 0) {
            QuizDirection.EN_TO_FR
        } else {
            QuizDirection.FR_TO_EN
        }

        val distractorCount = optionCount - 1
        val distractors = loadDistractors(dueWord, distractorCount)

        val options = buildList {
            add(
                QuizOption(
                    id = dueWord.id,
                    text = if (direction == QuizDirection.EN_TO_FR) dueWord.french else dueWord.english,
                    isCorrect = true
                )
            )
            distractors.forEachIndexed { index, distractor ->
                add(
                    QuizOption(
                        id = -(index + 1),
                        text = if (direction == QuizDirection.EN_TO_FR) distractor.french else distractor.english,
                        isCorrect = false
                    )
                )
            }
        }.shuffled()

        val prompt = when (direction) {
            QuizDirection.EN_TO_FR -> "Quelle est la traduction française de \"${dueWord.english}\" ?"
            QuizDirection.FR_TO_EN -> "What is the English translation of \"${dueWord.french}\"?"
        }

        return QuizQuestion(
            word = dueWord,
            prompt = prompt,
            direction = direction,
            options = options
        )
    }

    private suspend fun loadDistractors(word: Word, count: Int): List<Word> {
        if (count <= 0) return emptyList()
        val themed = wordDao.getRandomWordsFromTheme(word.theme, word.id, count).map { it.toModel() }
        return if (themed.size >= count) {
            themed
        } else {
            val remaining = count - themed.size
            val additional = wordDao.getRandomWords(word.id, remaining).map { it.toModel() }
            (themed + additional).take(count)
        }
    }

    suspend fun recordAnswer(word: Word, isCorrect: Boolean, answeredAt: Long = System.currentTimeMillis()) {
        val current = wordDao.getWordById(word.id) ?: return
        val newStep = if (isCorrect) {
            min(current.srsStep + 1, srsIntervalsDays.lastIndex)
        } else {
            max(current.srsStep - 1, 0)
        }
        val intervalDays = srsIntervalsDays[newStep]
        val nextReview = answeredAt + intervalDays * MILLIS_IN_DAY
        val updated = current.copy(
            srsStep = newStep,
            nextReviewEpochMillis = nextReview,
            lastReviewEpochMillis = answeredAt,
            successCount = if (isCorrect) current.successCount + 1 else current.successCount,
            failureCount = if (!isCorrect) current.failureCount + 1 else current.failureCount
        )
        wordDao.update(updated)
    }

    suspend fun addWord(
        english: String,
        french: String,
        theme: String,
        example: String?,
        exampleFrench: String?
    ) {
        val now = System.currentTimeMillis()
        val entity = WordEntity(
            english = english.trim(),
            french = french.trim(),
            theme = theme.trim(),
            example = example?.takeIf { it.isNotBlank() },
            exampleFrench = exampleFrench?.takeIf { it.isNotBlank() },
            srsStep = 0,
            nextReviewEpochMillis = now,
            lastReviewEpochMillis = null,
            successCount = 0,
            failureCount = 0
        )
        wordDao.insert(entity)
    }

    private fun WordEntity.toModel(): Word = Word(
        id = id,
        english = english,
        french = french,
        theme = theme,
        example = example,
        exampleFrench = exampleFrench,
        srsStep = srsStep,
        nextReviewEpochMillis = nextReviewEpochMillis,
        lastReviewEpochMillis = lastReviewEpochMillis,
        successCount = successCount,
        failureCount = failureCount
    )

    companion object {
        private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
    }
}

data class SeedWord(
    val english: String,
    val french: String,
    val theme: String,
    val example: String? = null,
    val exampleFrench: String? = null
)
