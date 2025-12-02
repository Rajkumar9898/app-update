package com.example.kioskapp.ui.theme

import MainScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kioskapp.UpdateManager
import kotlinx.coroutines.delay

@Composable
fun UpdateAppFlow(updateManager: UpdateManager) {

    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Update downloaded. Click reload to apply.",
                actionLabel = "Reload",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                updateManager.completeUpdate()
            }
            showSnackbar = false
        }
    }

    LaunchedEffect(Unit) {
        updateManager.listenForInstallUpdates {
            showSnackbar = true
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            updateManager.checkFlexibleUpdate()
            delay(30000L) // check every 30 seconds
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            MainScreen()
        }
    }
}

