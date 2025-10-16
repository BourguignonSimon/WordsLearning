package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Update
    suspend fun update(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Int): WordEntity?

    @Query("SELECT DISTINCT theme FROM words ORDER BY theme")
    fun observeThemes(): Flow<List<String>>

    @Query("SELECT * FROM words WHERE theme IN (:themes) ORDER BY english")
    fun observeWordsByThemes(themes: List<String>): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY english")
    fun observeAllWords(): Flow<List<WordEntity>>

    @Query(
        "SELECT * FROM words WHERE (:hasThemes = 0 OR theme IN (:themes)) " +
            "AND next_review_epoch <= :now ORDER BY next_review_epoch ASC LIMIT :limit"
    )
    suspend fun getDueWords(
        themes: List<String>,
        now: Long,
        limit: Int,
        hasThemes: Int
    ): List<WordEntity>

    @Query(
        "SELECT * FROM words WHERE (:hasThemes = 0 OR theme IN (:themes)) " +
            "ORDER BY next_review_epoch ASC LIMIT :limit"
    )
    suspend fun getUpcomingWords(
        themes: List<String>,
        limit: Int,
        hasThemes: Int
    ): List<WordEntity>

    @Query(
        "SELECT * FROM words WHERE theme = :theme AND id != :excludeId ORDER BY RANDOM() LIMIT :count"
    )
    suspend fun getRandomWordsFromTheme(
        theme: String,
        excludeId: Int,
        count: Int
    ): List<WordEntity>

    @Query("SELECT * FROM words WHERE id != :excludeId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWords(excludeId: Int, count: Int): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE next_review_epoch <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM words")
    fun observeTotalCount(): Flow<Int>
}
