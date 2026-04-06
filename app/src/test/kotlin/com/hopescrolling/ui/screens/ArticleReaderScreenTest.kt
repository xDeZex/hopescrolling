package com.hopescrolling.ui.screens

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.hopescrolling.data.article.ArticleContent
import com.hopescrolling.util.FakeArticleContentFetcher
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
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ArticleReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun readerScreen_showsOpenInBrowserButtonOnError() {
        val viewModel = ArticleReaderViewModel(
            FakeArticleContentFetcher(Result.failure(RuntimeException("connection refused"))),
            "https://example.com/article",
        )
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_error").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reader_open_in_browser").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reader_open_in_browser").assertHasClickAction()
    }

    @Test
    fun readerScreen_showsToastWhenNoBrowserAppFound() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        Shadows.shadowOf(app).checkActivities(true)

        val viewModel = ArticleReaderViewModel(
            FakeArticleContentFetcher(Result.failure(RuntimeException("connection refused"))),
            "https://example.com/article",
        )
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_open_in_browser").performClick()

        assert(ShadowToast.getTextOfLatestToast() == "No browser app found")
    }

    @Test
    fun readerScreen_showsTitleAndParagraphsOnSuccess() {
        val content = ArticleContent(title = "Great Article", paragraphs = listOf("First paragraph.", "Second paragraph."))
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(Result.success(content)), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Great Article").assertIsDisplayed()
        composeTestRule.onNodeWithText("First paragraph.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second paragraph.").assertIsDisplayed()
    }

    @Test
    fun readerScreen_showsLoadingIndicatorWhileLoading() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_loading").assertIsDisplayed()
    }
}
