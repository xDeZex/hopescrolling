package com.hopescrolling.util

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.feed.FeedSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFeedSourceRepository : FeedSourceRepository {
    val sources = MutableStateFlow<List<FeedSource>>(emptyList())

    override fun getAll(): Flow<List<FeedSource>> = sources

    override suspend fun add(source: FeedSource) {
        sources.value = sources.value + source
    }

    override suspend fun remove(id: String) {
        sources.value = sources.value.filter { it.id != id }
    }

    override suspend fun update(source: FeedSource) {
        sources.value = sources.value.map { if (it.id == source.id) source else it }
    }
}
