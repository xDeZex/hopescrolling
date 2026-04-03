package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.rss.Article
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimelineUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class TimelineViewModel(
    private val repository: ArticleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState
    private var fetchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        fetchJob?.cancel()
        _uiState.value = TimelineUiState(isLoading = true)
        fetchJob = viewModelScope.launch {
            runCatching { repository.getArticles() }
                .onSuccess { articles ->
                    _uiState.value = TimelineUiState(articles = articles, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = TimelineUiState(isLoading = false, error = e.message ?: e::class.simpleName ?: "Unknown error")
                }
        }
    }
}
