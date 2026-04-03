package com.hopescrolling.data.readstate

import kotlinx.coroutines.flow.Flow

interface ReadStateRepository {
    fun getReadIds(): Flow<Set<String>>
    suspend fun markRead(articleId: String)
}
