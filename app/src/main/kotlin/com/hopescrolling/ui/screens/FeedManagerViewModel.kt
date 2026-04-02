package com.hopescrolling.ui.screens

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.feed.FeedSourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class FeedManagerViewModel(
    private val repository: FeedSourceRepository,
    private val scope: CoroutineScope,
) {
    val feedSources: StateFlow<List<FeedSource>> = repository.getAll()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun addFeed(url: String) {
        scope.launch {
            repository.add(FeedSource(id = UUID.randomUUID().toString(), name = url, url = url))
        }
    }

    fun deleteFeed(id: String) {
        scope.launch { repository.remove(id) }
    }

    fun renameFeed(id: String, newName: String) {
        val current = feedSources.value.firstOrNull { it.id == id } ?: return
        scope.launch { repository.update(current.copy(name = newName)) }
    }
}
