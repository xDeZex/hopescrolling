package com.hopescrolling.data.update

sealed interface UpdateState {
    object Loading : UpdateState
    object UpToDate : UpdateState
    data class UpdateAvailable(val latestLabel: String, val apkUrl: String) : UpdateState
    object Error : UpdateState
}

interface AppUpdateRepository {
    suspend fun getUpdateState(): UpdateState
}
