package com.example.kioskapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kioskapp.ui.theme.UpdateAppFlow

class MainActivity : ComponentActivity() {

    private lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UpdateManager
        updateManager = UpdateManager(this)

        setContent {
            // Pass updateManager to Compose
            UpdateAppFlow(updateManager)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateManager.onActivityResultHandled(requestCode)
    }
}
