package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application racine qui installe l'IoC [AppContainer] et déclenche l'initialisation
 * asynchrone de la base Room avec le jeu de 2000 mots.
 */
class WordsLearningApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.wordsInitializer.ensureSeedData()
        }
    }
}
