package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.readstate.ReadStateRepository
import com.hopescrolling.data.rss.Article
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimelineUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val readIds: Set<String> = emptySet(),
)

private data class ArticlesLoadState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class TimelineViewModel(
    private val repository: ArticleRepository,
    private val readStateRepository: ReadStateRepository,
) : ViewModel() {
    private val _articlesState = MutableStateFlow(ArticlesLoadState())

    val uiState: StateFlow<TimelineUiState> = combine(
        _articlesState,
        readStateRepository.getReadIds(),
    ) { articlesState, readIds ->
        TimelineUiState(
            articles = articlesState.articles,
            isLoading = articlesState.isLoading,
            error = articlesState.error,
            readIds = readIds,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TimelineUiState())

    private var fetchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        fetchJob?.cancel()
        _articlesState.update { it.copy(isLoading = true, error = null) }
        fetchJob = viewModelScope.launch {
            runCatching { repository.getArticles() }
                .onSuccess { articles ->
                    _articlesState.update { it.copy(articles = articles, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _articlesState.update {
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
