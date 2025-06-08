package com.guri.guriresumerewrite

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI

class GuriResumeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
} 