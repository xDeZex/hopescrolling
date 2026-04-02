package com.hopescrolling.ui.screens

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.util.FakeFeedSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FeedManagerViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `renameFeed updates the name of the source with the given id`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Old Name", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = FeedManagerViewModel(repo)

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
        val viewModel = FeedManagerViewModel(repo)

        viewModel.deleteFeed("1")

        assertEquals(emptyList<FeedSource>(), viewModel.feedSources.first())
    }

    @Test
    fun `addFeed adds a source with the given url`() = runTest {
        val repo = FakeFeedSourceRepository()
        val viewModel = FeedManagerViewModel(repo)

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
        val viewModel = FeedManagerViewModel(repo)

        assertEquals(listOf(source), viewModel.feedSources.first())
    }
}
