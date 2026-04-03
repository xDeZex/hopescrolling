package com.hopescrolling.ui.screens

import com.hopescrolling.data.rss.Article
import com.hopescrolling.util.FakeArticleRepository
import com.hopescrolling.util.FakeReadStateRepository
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
class TimelineViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `uiState initially has isLoading true`() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val repo = FakeArticleRepository()
        val viewModel = TimelineViewModel(repo, FakeReadStateRepository())

        assertEquals(true, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState emits articles and isLoading false after load`() = runTest {
        val articles = listOf(
            Article(title = "First", link = "https://a.com/1", description = "Desc", pubDate = null, feedSourceId = "f1"),
            Article(title = "Second", link = "https://a.com/2", description = null, pubDate = null, feedSourceId = "f1"),
        )
        val repo = FakeArticleRepository(articles = articles)
        val viewModel = TimelineViewModel(repo, FakeReadStateRepository())

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(articles, state.articles)
        assertEquals(null, state.error)
    }

    @Test
    fun `uiState emits error and isLoading false when repository throws`() = runTest {
        val repo = FakeArticleRepository(error = RuntimeException("network failure"))
        val viewModel = TimelineViewModel(repo, FakeReadStateRepository())

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(emptyList<Any>(), state.articles)
        assertEquals("network failure", state.error)
    }

    @Test
    fun `uiState emits non-null error when exception has no message`() = runTest {
        val repo = FakeArticleRepository(error = RuntimeException())
        val viewModel = TimelineViewModel(repo, FakeReadStateRepository())

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(false, state.error.isNullOrBlank())
    }

    @Test
    fun `refresh resets state to loading while fetching`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repo = FakeArticleRepository()
            val viewModel = TimelineViewModel(repo, FakeReadStateRepository())
            testScheduler.advanceUntilIdle() // complete init fetch

            viewModel.refresh()

            assertEquals(true, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        } finally {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        }
    }

    @Test
    fun `refresh re-fetches and emits articles`() = runTest {
        val articles = listOf(
            Article(title = "Refreshed", link = "https://a.com/1", description = null, pubDate = null, feedSourceId = "f1"),
        )
        val repo = FakeArticleRepository(articles = articles)
        val viewModel = TimelineViewModel(repo, FakeReadStateRepository())
        viewModel.uiState.first { !it.isLoading } // wait for init
        val countBeforeRefresh = repo.callCount

        viewModel.refresh()
        viewModel.uiState.first { !it.isLoading }

        assertEquals(countBeforeRefresh + 1, repo.callCount)
        assertEquals(articles, viewModel.uiState.value.articles)
    }

    @Test
    fun `uiState readIds is empty when no articles have been read`() = runTest {
        val viewModel = TimelineViewModel(FakeArticleRepository(), FakeReadStateRepository())

        val state = viewModel.uiState.first()

        assertEquals(emptySet<String>(), state.readIds)
    }

    @Test
    fun `markRead adds article id to uiState readIds`() = runTest {
        val viewModel = TimelineViewModel(FakeArticleRepository(), FakeReadStateRepository())

        viewModel.markRead("https://a.com/1")

        assertEquals(setOf("https://a.com/1"), viewModel.uiState.value.readIds)
    }
}
