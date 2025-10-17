package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppContainer

class WordsLearningApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
