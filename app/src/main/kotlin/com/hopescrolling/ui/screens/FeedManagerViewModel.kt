package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.feed.FeedSourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class FeedManagerViewModel(
    private val repository: FeedSourceRepository,
) : ViewModel() {
    val feedSources: StateFlow<List<FeedSource>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addFeed(url: String) {
        viewModelScope.launch {
            repository.add(FeedSource(id = UUID.randomUUID().toString(), name = url, url = url))
        }
    }

    fun deleteFeed(id: String) {
        viewModelScope.launch { repository.remove(id) }
    }

    fun renameFeed(id: String, newName: String) {
        val current = feedSources.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch { repository.update(current.copy(name = newName)) }
    }
}
