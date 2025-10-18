package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.local.WordsDatabase

/**
 * Point d'entrée d'injection très simple pour exposer les singletons de l'application.
 *
 * Il instancie la base Room, le [WordsRepository] et les utilitaires d'initialisation
 * utilisés dans le reste de l'application.
 */
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
