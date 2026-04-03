package com.hopescrolling.util

import com.hopescrolling.data.readstate.ReadStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReadStateRepository : ReadStateRepository {
    private val readIds = MutableStateFlow<Set<String>>(emptySet())

    override fun getReadIds(): Flow<Set<String>> = readIds

    override suspend fun markRead(articleId: String) {
        readIds.value = readIds.value + articleId
    }
}
