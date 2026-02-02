package com.reprolog.autoupdate

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    companion object {
        private const val UPDATE_REQUEST_CODE = 1001
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private var isDownloadStarted by mutableStateOf(false)
    private var isUpdateFlowInProgress by mutableStateOf(false)
    private var updateCheckTime = 1 * 60 * 1000L // 1 minute

    private val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                isDownloadStarted = true
            }

            InstallStatus.DOWNLOADED -> {
                isDownloadStarted = false
                appUpdateManager.completeUpdate()
                isUpdateFlowInProgress = false
            }

            InstallStatus.CANCELED, InstallStatus.FAILED -> {
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
                delay(updateCheckTime)
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
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    isUpdateFlowInProgress = false
                } catch (t: Throwable) {
                    t.printStackTrace()
                    isUpdateFlowInProgress = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // When coming back to the activity, query current update state to resume or finish the flow
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    // If downloaded but not completed, finish the update
                    appUpdateManager.completeUpdate()
                    isUpdateFlowInProgress = false
                }
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // Resume update flow if it was in progress
                    try {
                        isUpdateFlowInProgress = true
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            this,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                            UPDATE_REQUEST_CODE
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                        isUpdateFlowInProgress = false
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        isUpdateFlowInProgress = false
                    }
                }
                else -> {
                    // No active update
                    isUpdateFlowInProgress = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            // If the user canceled the update flow, clear the flag so future checks can run
            if (resultCode != RESULT_OK) {
                isUpdateFlowInProgress = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(listener)
    }

}