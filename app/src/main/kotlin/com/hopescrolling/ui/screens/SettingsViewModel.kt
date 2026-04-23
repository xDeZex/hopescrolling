package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.feed.FeedSourceRepository
import com.hopescrolling.data.update.AppUpdateRepository
import com.hopescrolling.data.update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val repository: FeedSourceRepository,
    private val appUpdateRepository: AppUpdateRepository,
) : ViewModel() {
    val feedSources: StateFlow<List<FeedSource>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Loading)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    init {
        viewModelScope.launch {
            _updateState.value = appUpdateRepository.getUpdateState()
        }
    }

    fun addFeed(url: String) {
        viewModelScope.launch {
            val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            repository.add(FeedSource(id = UUID.randomUUID().toString(), name = normalizedUrl, url = normalizedUrl))
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
