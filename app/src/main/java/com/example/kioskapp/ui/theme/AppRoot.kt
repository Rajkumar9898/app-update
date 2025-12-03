package com.reprolog.autoupdate

import android.content.pm.PackageInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManager

@Composable
fun AppRoot (appUpdateManager: AppUpdateManager, showRestartSnack: Boolean){
    val snackHostState = SnackbarHostState()
    val packageInfo: PackageInfo? =
        LocalContext.current.packageManager.getPackageInfo(
            LocalContext.current.applicationContext.packageName,
            0
        )
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
}

