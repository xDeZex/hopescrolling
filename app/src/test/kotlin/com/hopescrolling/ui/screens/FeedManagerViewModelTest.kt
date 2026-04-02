package com.hopescrolling.ui.screens

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.feed.FeedSourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedManagerViewModelTest {

    private class FakeFeedSourceRepository : FeedSourceRepository {
        private val sources = MutableStateFlow<List<FeedSource>>(emptyList())

        override fun getAll(): Flow<List<FeedSource>> = sources

        override suspend fun add(source: FeedSource) {
            sources.value = sources.value + source
        }

        override suspend fun remove(id: String) {
            sources.value = sources.value.filter { it.id != id }
        }

        override suspend fun update(source: FeedSource) {
            sources.value = sources.value.map { if (it.id == source.id) source else it }
        }
    }

    private fun viewModelScope() = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `renameFeed updates the name of the source with the given id`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Old Name", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = FeedManagerViewModel(repo, viewModelScope())

        viewModel.renameFeed("1", "New Name")

        val updated = viewModel.feedSources.first().first { it.id == "1" }
        assertEquals("New Name", updated.name)
        assertEquals("https://example.com/feed", updated.url)
    }

    @Test
    fun `deleteFeed removes the source with the given id`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Feed", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = FeedManagerViewModel(repo, viewModelScope())

        viewModel.deleteFeed("1")

        assertEquals(emptyList<FeedSource>(), viewModel.feedSources.first())
    }

    @Test
    fun `addFeed adds a source with the given url`() = runTest {
        val repo = FakeFeedSourceRepository()
        val viewModel = FeedManagerViewModel(repo, viewModelScope())

        viewModel.addFeed("https://example.com/feed")

        val sources = viewModel.feedSources.first()
        assertEquals(1, sources.size)
        assertEquals("https://example.com/feed", sources[0].url)
    }

    @Test
    fun `feedSources emits the list from the repository`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Test Feed", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = FeedManagerViewModel(repo, viewModelScope())

        assertEquals(listOf(source), viewModel.feedSources.first())
    }
}
