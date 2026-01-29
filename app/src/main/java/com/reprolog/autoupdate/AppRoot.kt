package com.reprolog.autoupdate

import android.content.pm.PackageInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppRoot(downloading: Boolean, checking: Boolean) {
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


    if (downloading || checking) {
        UpdateDownloadingScreen(downloading)
    }
}

@Composable
fun UpdateDownloadingScreen(downloading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    if (downloading) Color.White else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(24.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(60.dp), strokeWidth = 6.dp)
            if (downloading) {
                Text("Downloading Update")
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DownloadingBox() {
    AppRoot(true, true)
}
