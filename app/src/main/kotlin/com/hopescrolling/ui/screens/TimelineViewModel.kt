package com.hopescrolling.ui.screens

import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.rss.Article
import kotlinx.coroutines.CoroutineScope
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
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState

    init {
        scope.launch {
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
