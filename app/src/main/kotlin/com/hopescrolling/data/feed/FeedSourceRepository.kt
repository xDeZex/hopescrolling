package com.hopescrolling.data.feed

import kotlinx.coroutines.flow.Flow

interface FeedSourceRepository {
    fun getAll(): Flow<List<FeedSource>>
    suspend fun add(source: FeedSource)
    suspend fun remove(id: String)
    suspend fun update(source: FeedSource)
}
