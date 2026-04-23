package com.hopescrolling.ui.screens

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.update.UpdateState
import com.hopescrolling.util.FakeAppUpdateRepository
import com.hopescrolling.util.FakeFeedSourceRepository
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
class SettingsViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `feedSources emits the list from the repository`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Test Feed", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())

        assertEquals(listOf(source), viewModel.feedSources.first())
    }

    @Test
    fun `addFeed adds a source with the given url`() = runTest {
        val repo = FakeFeedSourceRepository()
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())

        viewModel.addFeed("https://example.com/feed")

        val sources = viewModel.feedSources.first()
        assertEquals(1, sources.size)
        assertEquals("https://example.com/feed", sources[0].url)
    }

    @Test
    fun `deleteFeed removes the source with the given id`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Feed", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())

        viewModel.deleteFeed("1")

        assertEquals(emptyList<FeedSource>(), viewModel.feedSources.first())
    }

    @Test
    fun `addFeed prepends https when url has no scheme`() = runTest {
        val repo = FakeFeedSourceRepository()
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())

        viewModel.addFeed("example.com/feed")

        val sources = viewModel.feedSources.first()
        assertEquals(1, sources.size)
        assertEquals("https://example.com/feed", sources[0].url)
        assertEquals("https://example.com/feed", sources[0].name)
    }

    @Test
    fun `renameFeed updates the name of the source with the given id`() = runTest {
        val repo = FakeFeedSourceRepository()
        val source = FeedSource(id = "1", name = "Old Name", url = "https://example.com/feed")
        repo.add(source)
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())

        viewModel.renameFeed("1", "New Name")

        val updated = viewModel.feedSources.first().first { it.id == "1" }
        assertEquals("New Name", updated.name)
        assertEquals("https://example.com/feed", updated.url)
    }

    @Test
    fun `updateState is Loading immediately before check completes`() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository())

        assertEquals(UpdateState.Loading, viewModel.updateState.value)
    }

    @Test
    fun `updateState transitions to UpToDate when repository returns UpToDate`() = runTest {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(UpdateState.UpToDate))

        val state = viewModel.updateState.first { it != UpdateState.Loading }

        assertEquals(UpdateState.UpToDate, state)
    }

    @Test
    fun `updateState transitions to UpdateAvailable when update exists`() = runTest {
        val expected = UpdateState.UpdateAvailable("v2", "https://example.com/app.apk")
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(expected))

        val state = viewModel.updateState.first { it != UpdateState.Loading }

        assertEquals(expected, state)
    }

    @Test
    fun `updateState transitions to Error when repository returns Error`() = runTest {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(UpdateState.Error))

        val state = viewModel.updateState.first { it != UpdateState.Loading }

        assertEquals(UpdateState.Error, state)
    }

    @Test
    fun `isDownloading is false initially`() = runTest {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository())

        assertEquals(false, viewModel.isDownloading.value)
    }

    @Test
    fun `startDownload sets isDownloading to true`() = runTest {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository())

        viewModel.startDownload()

        assertEquals(true, viewModel.isDownloading.value)
    }
}
