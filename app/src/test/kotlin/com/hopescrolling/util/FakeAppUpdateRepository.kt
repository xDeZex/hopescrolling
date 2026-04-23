package com.hopescrolling.util

import com.hopescrolling.data.update.AppUpdateRepository
import com.hopescrolling.data.update.UpdateState

class FakeAppUpdateRepository(
    private val result: UpdateState = UpdateState.UpToDate,
) : AppUpdateRepository {
    override suspend fun getUpdateState(): UpdateState = result
}
