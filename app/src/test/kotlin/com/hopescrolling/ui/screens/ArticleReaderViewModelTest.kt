package com.hopescrolling.ui.screens

import com.hopescrolling.data.article.ArticleContent
import com.hopescrolling.util.FakeArticleContentFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ArticleReaderViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `uiState emits Error after failed fetch`() = runTest {
        val url = "https://example.com/article"
        val viewModel = ArticleReaderViewModel(
            FakeArticleContentFetcher(Result.failure(RuntimeException("network error"))),
            url,
        )

        val state = viewModel.uiState.first { it is ArticleReaderUiState.Error } as ArticleReaderUiState.Error

        assertEquals("network error", state.message)
        assertEquals(url, state.url)
    }

    @Test
    fun `uiState emits Success with content after successful fetch`() = runTest {
        val content = ArticleContent(title = "My Title", paragraphs = listOf("Para 1", "Para 2"))
        val viewModel = ArticleReaderViewModel(
            FakeArticleContentFetcher(Result.success(content)),
            "https://example.com/article",
        )

        val state = viewModel.uiState.first { it is ArticleReaderUiState.Success }

        assertEquals(ArticleReaderUiState.Success(content), state)
    }

    @Test
    fun `uiState is Loading before fetch completes`() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(), "https://example.com/article")

        assertEquals(ArticleReaderUiState.Loading, viewModel.uiState.value)
    }
}
