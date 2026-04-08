package com.hopescrolling.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopescrolling.data.article.ArticleContent
import com.hopescrolling.data.article.ArticleContentFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ArticleReaderUiState {
    data object Loading : ArticleReaderUiState
    data class Success(val content: ArticleContent) : ArticleReaderUiState
    data class Error(val message: String) : ArticleReaderUiState
}

class ArticleReaderViewModel(
    private val fetcher: ArticleContentFetcher,
    private val url: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArticleReaderUiState>(ArticleReaderUiState.Loading)
    val uiState: StateFlow<ArticleReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = fetcher.fetch(url).fold(
                onSuccess = { ArticleReaderUiState.Success(it) },
                onFailure = { ArticleReaderUiState.Error(it.message ?: "Failed to load article") },
            )
        }
    }
}
