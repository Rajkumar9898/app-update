package com.reprolog.autoupdate

import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showRestartSnack = true
        }
    }

    private var showRestartSnack by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)

        setContent {
            AppRoot(appUpdateManager, showRestartSnack)
            PeriodicUpdateChecker(appUpdateManager)
        }
    }

    @Composable
    fun PeriodicUpdateChecker(appUpdateManager: AppUpdateManager) {
        LaunchedEffect(Unit) {
            while (true) {
                checkForUpdate(appUpdateManager)
                kotlinx.coroutines.delay(15 * 60 * 1000L)
            }
        }
    }

    private fun checkForUpdate(appUpdateManager: AppUpdateManager) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val available =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (available) {
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

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }
}
