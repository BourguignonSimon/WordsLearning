package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.local.WordsDatabase

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    private val database: WordsDatabase by lazy {
        WordsDatabase.build(applicationContext)
    }

    val wordsRepository: WordsRepository by lazy {
        WordsRepository(database.wordDao())
    }

    private val seedWordLoader: SeedWordLoader by lazy {
        SeedWordLoader(applicationContext)
    }

    val wordsInitializer: WordsInitializer by lazy {
        WordsInitializer(applicationContext, wordsRepository, seedWordLoader)
    }
}
