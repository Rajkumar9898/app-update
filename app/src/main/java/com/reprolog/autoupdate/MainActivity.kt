package com.reprolog.autoupdate

import android.content.IntentSender
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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
    private val snackHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val packageInfo: PackageInfo? =
            this.applicationContext.packageManager.getPackageInfo(
                this.applicationContext.packageName,
                0
            )

        setContent {
            Scaffold(snackbarHost = { SnackbarHost(snackHostState) }) {
                it
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Version no: ${packageInfo?.versionName}(${packageInfo?.longVersionCode})",
                        fontSize = 45.sp
                    )
                }
            }
            if (showRestartSnack) {
                RestartSnack()
            }
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


    @Composable
    fun RestartSnack() {
        LaunchedEffect(Unit) {
            val result = snackHostState.showSnackbar(
                message = "Update downloaded. Restart?",
                actionLabel = "Restart",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }
}
