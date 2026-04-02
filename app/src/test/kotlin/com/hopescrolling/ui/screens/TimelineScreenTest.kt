package com.hopescrolling.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.hopescrolling.data.rss.Article
import com.hopescrolling.util.FakeArticleRepository
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
class TimelineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun timelineScreen_showsLoadingIndicatorWhileLoading() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val repo = FakeArticleRepository()
        val viewModel = TimelineViewModel(repo)
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("timeline_loading").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_showsArticleTitles() {
        val articles = listOf(
            Article(title = "First Post", link = "https://a.com/1", description = null, pubDate = null, feedSourceId = "f1"),
            Article(title = "Second Post", link = "https://a.com/2", description = null, pubDate = null, feedSourceId = "f1"),
        )
        val viewModel = TimelineViewModel(FakeArticleRepository(articles = articles))
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithText("First Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Post").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_showsArticleDescription() {
        val articles = listOf(
            Article(title = "Post With Desc", link = "https://a.com/1", description = "A summary of the post", pubDate = null, feedSourceId = "f1"),
        )
        val viewModel = TimelineViewModel(FakeArticleRepository(articles = articles))
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithText("A summary of the post").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_showsErrorMessage() {
        val repo = FakeArticleRepository(error = RuntimeException("could not load"))
        val viewModel = TimelineViewModel(repo)
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("timeline_error").assertIsDisplayed()
        composeTestRule.onNodeWithText("could not load").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_showsEmptyStateWhenNoArticles() {
        val viewModel = TimelineViewModel(FakeArticleRepository(articles = emptyList()))
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("timeline_empty").assertIsDisplayed()
    }
}
