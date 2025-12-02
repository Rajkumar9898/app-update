package com.example.kioskapp

import android.app.Activity
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.model.*

class UpdateManager(private val activity: Activity) {

    companion object {
        const val UPDATE_REQUEST_CODE = 1234
    }

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private var isDialogShowing = false

    fun checkFlexibleUpdate() {
        if (isDialogShowing) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    isDialogShowing = true
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    isDialogShowing = false
                }
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun onActivityResultHandled(requestCode: Int) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            isDialogShowing = false
        }
    }

    fun listenForInstallUpdates(onDownloaded: () -> Unit) {
        appUpdateManager.registerListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onDownloaded()
            }
        }
    }
}
