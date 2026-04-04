package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.readstate.ReadStateRepository
import com.hopescrolling.data.rss.Article
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimelineUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val readIds: Set<String> = emptySet(),
)

class TimelineViewModel(
    private val repository: ArticleRepository,
    private val readStateRepository: ReadStateRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch {
            readStateRepository.getReadIds().collect { readIds ->
                _uiState.update { it.copy(readIds = readIds) }
            }
        }
        refresh()
    }

    fun refresh() {
        fetchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchJob = viewModelScope.launch {
            runCatching { repository.getArticles() }
                .onSuccess { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: e::class.simpleName ?: "Unknown error",
                        )
                    }
                }
        }
    }

    fun markRead(articleId: String) {
        viewModelScope.launch {
            readStateRepository.markRead(articleId)
        }
    }
}
