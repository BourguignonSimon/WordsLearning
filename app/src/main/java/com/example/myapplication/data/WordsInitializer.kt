package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.seedDataStore: DataStore<Preferences> by preferencesDataStore(name = "words_learning_seed")

/**
 * Assure le chargement unique des données seed dans la base Room.
 *
 * Un flag stocké dans DataStore évite toute ré-initialisation accidentelle,
 * ce qui rend cette classe idempotente au redémarrage de l'application.
 */
class WordsInitializer(
    private val context: Context,
    private val repository: WordsRepository,
    private val seedWordLoader: SeedWordLoader
) {

    private val initializedKey = preferencesKey<Boolean>("seed_initialized")

    suspend fun ensureSeedData() {
        val dataStore = context.seedDataStore
        val prefs = dataStore.data.first()
        if (prefs[initializedKey] == true) {
            return
        }

        val seedWords = seedWordLoader.loadSeedWords()
        if (seedWords.isNotEmpty()) {
            repository.insertSeedWords(seedWords)
        }

        dataStore.edit { settings ->
            settings[initializedKey] = true
        }
    }
}
