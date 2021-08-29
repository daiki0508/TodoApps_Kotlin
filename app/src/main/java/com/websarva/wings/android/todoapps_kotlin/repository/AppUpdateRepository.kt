package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestAppUpdateInfo

interface AppUpdateRepository {
    suspend fun appUpdate(activity: Activity)
    suspend fun restartUpdate(activity: Activity): Boolean
}

class AppUpdateRepositoryClient: AppUpdateRepository {
    override suspend fun appUpdate(activity: Activity){
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
            AppUpdateResult.Available(appUpdateManager, appUpdateInfo).startImmediateUpdate(
                activity,
                100
            )
        }
    }

    override suspend fun restartUpdate(activity: Activity): Boolean {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()

        when(appUpdateInfo.updateAvailability()){
            UpdateAvailability.UPDATE_AVAILABLE, UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                -> AppUpdateResult.Available(appUpdateManager, appUpdateInfo)
                .startImmediateUpdate(activity, 100)

            else -> return false
        }
        return true
    }
}