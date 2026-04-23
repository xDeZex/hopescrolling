package com.hopescrolling.ui.screens

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.update.UpdateState
import com.hopescrolling.util.FakeAppUpdateRepository
import com.hopescrolling.util.FakeFeedSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun settingsScreen_showsFeedSourceNames() {
        val repo = FakeFeedSourceRepository()
        repo.sources.value = listOf(
            FeedSource(id = "1", name = "Tech Blog", url = "https://tech.example.com/feed"),
            FeedSource(id = "2", name = "News Feed", url = "https://news.example.com/feed"),
        )
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithText("Tech Blog").assertIsDisplayed()
        composeTestRule.onNodeWithText("News Feed").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hasAddInputAndButton() {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("add_feed_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_feed_button").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_addingUrlAppearsInList() {
        val repo = FakeFeedSourceRepository()
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("add_feed_input").performTextInput("https://example.com/rss")
        composeTestRule.onNodeWithTag("add_feed_button").performClick()

        composeTestRule.onNodeWithText("https://example.com/rss").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_deleteButtonRemovesItem() {
        val repo = FakeFeedSourceRepository()
        repo.sources.value = listOf(
            FeedSource(id = "1", name = "Tech Blog", url = "https://tech.example.com/feed"),
        )
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("delete_feed_1").performClick()

        composeTestRule.onNodeWithText("Tech Blog").assertDoesNotExist()
    }

    @Test
    fun settingsScreen_renameDialogUpdatesName() {
        val repo = FakeFeedSourceRepository()
        repo.sources.value = listOf(
            FeedSource(id = "1", name = "Old Name", url = "https://example.com/feed"),
        )
        val viewModel = SettingsViewModel(repo, FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("rename_feed_1").performClick()
        composeTestRule.onNodeWithTag("rename_dialog_input").assert(hasText("Old Name"))
        composeTestRule.onNodeWithTag("rename_dialog_input")
            .performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString("New Name")) }
        composeTestRule.onNodeWithTag("rename_dialog_confirm").performClick()

        composeTestRule.onNodeWithText("New Name").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsLoadingIndicatorDuringUpdateCheck() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository())
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("update_loading").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsUpToDateWhenNoUpdateAvailable() {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(UpdateState.UpToDate))
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("update_up_to_date").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsVersionLabelsWhenUpdateAvailable() {
        val state = UpdateState.UpdateAvailable(latestLabel = "Build 42", apkUrl = "https://example.com/app.apk")
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(state))
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("update_installed_version").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_latest_version").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsErrorMessageOnUpdateCheckFailure() {
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(UpdateState.Error))
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("update_error").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsDownloadButtonWhenUpdateAvailable() {
        val state = UpdateState.UpdateAvailable(latestLabel = "Build 42", apkUrl = "https://example.com/app.apk")
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(state))
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("update_download_button").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_downloadButtonDisabledAfterClick() {
        val state = UpdateState.UpdateAvailable(latestLabel = "Build 42", apkUrl = "https://example.com/app.apk")
        val viewModel = SettingsViewModel(FakeFeedSourceRepository(), FakeAppUpdateRepository(state))
        composeTestRule.setContent { SettingsScreen(viewModel = viewModel, onDownloadUpdate = {}) }

        composeTestRule.onNodeWithTag("update_download_button").performClick()

        composeTestRule.onNodeWithText("Downloading...").assertIsDisplayed()
    }
}
