package com.reprolog.autoupdate

import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.*

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private var showRestartSnack by mutableStateOf(false)
    private var isDownloadStarted by mutableStateOf(false)
    private val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                isDownloadStarted = true
            }
            InstallStatus.DOWNLOADED -> {
                isDownloadStarted = false
                showRestartSnack = true
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
                kotlinx.coroutines.delay(1 * 60 * 1000L) // 1 minute check evey minute from play store
            }
        }

        LaunchedEffect(isDownloadStarted) {
            if (!isDownloadStarted) return@LaunchedEffect
            while (isDownloadStarted) {
                checkDownloaded()
                kotlinx.coroutines.delay(5000L) // 5 second check after download
            }
        }
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (updateAvailable) {
                try {
                    appUpdateManager.startUpdateFlow(
                        info,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }

            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showRestartSnack = true
            }
        }
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
