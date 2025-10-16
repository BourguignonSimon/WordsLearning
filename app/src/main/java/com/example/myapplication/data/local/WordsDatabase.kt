package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WordsDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        fun build(context: Context): WordsDatabase = Room.databaseBuilder(
            context.applicationContext,
            WordsDatabase::class.java,
            "words_learning.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
