package com.example.kioskapp

import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.*
import com.reprolog.autoupdate.AppRoot
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private var showRestartSnack by mutableStateOf(false)
    private var isDownloadStarted by mutableStateOf(false)
    private var isUpdateFlowInProgress by mutableStateOf(false)

    private val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                isDownloadStarted = true
            }
            InstallStatus.DOWNLOADED -> {
                isDownloadStarted = false
                showRestartSnack = true
            }
            InstallStatus.CANCELED -> {
                isUpdateFlowInProgress = false
            }
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(listener)

        setContent {
            AppRoot(appUpdateManager, showRestartSnack)
            PeriodicUpdateChecker()
        }
    }

    @Composable
    fun PeriodicUpdateChecker() {
        LaunchedEffect(Unit) {
            while (true) {
                checkForUpdate()
                delay(1 * 60 * 1000L)
            }
        }

        LaunchedEffect(isDownloadStarted) {
            if (!isDownloadStarted) return@LaunchedEffect
            while (isDownloadStarted) {
                checkDownloaded()
                delay(5000L)
            }
        }
    }

    private fun checkForUpdate() {
        if (isUpdateFlowInProgress) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (updateAvailable) {
                try {
                    isUpdateFlowInProgress = true
                    appUpdateManager.startUpdateFlow(
                        info,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    isUpdateFlowInProgress = false
                }
            }

            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showRestartSnack = true
            }
        }
    }
    override fun onResume() {
        super.onResume()
        isUpdateFlowInProgress = false
    }


    private fun checkDownloaded() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                isDownloadStarted = false
                showRestartSnack = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }
}
