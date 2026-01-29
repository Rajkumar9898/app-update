package com.reprolog.autoupdate

import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private var isDownloadStarted by mutableStateOf(false)
    private var isUpdateFlowInProgress by mutableStateOf(false)
    private val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                isDownloadStarted = true
            }

            InstallStatus.DOWNLOADED -> {
                isDownloadStarted = false
                appUpdateManager.completeUpdate()
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
            AppRoot(isDownloadStarted, isUpdateFlowInProgress)
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
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(1000L)
            isUpdateFlowInProgress = false
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }
}
