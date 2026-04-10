package com.hopescrolling.ui.screens

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil.Coil
import coil.ImageLoader
import coil.test.FakeImageLoaderEngine
import com.hopescrolling.data.article.ArticleContent
import com.hopescrolling.util.FakeArticleContentFetcher
import kotlinx.coroutines.Dispatchers
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, coil.annotation.ExperimentalCoilApi::class)
@RunWith(RobolectricTestRunner::class)
class ArticleReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val engine = FakeImageLoaderEngine.Builder()
            .default(ColorDrawable(Color.TRANSPARENT))
            .build()
        Coil.setImageLoader(ImageLoader.Builder(context).components { add(engine) }.build())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Coil.reset()
    }

    @Test
    fun readerScreen_showsErrorMessage() {
        val viewModel = ArticleReaderViewModel(
            FakeArticleContentFetcher(Result.failure(RuntimeException("connection refused"))),
            "https://example.com/article",
        )
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_error").assertIsDisplayed()
    }

    @Test
    fun readerScreen_showsToastWhenNoBrowserAppFound() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        Shadows.shadowOf(app).checkActivities(true)

        val content = ArticleContent(
            title = "Article",
            paragraphs = listOf("Para"),
            links = listOf(com.hopescrolling.data.article.ArticleLink("A link", "https://example.com/ref")),
        )
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(Result.success(content)), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_link_0").performClick()

        assert(ShadowToast.getTextOfLatestToast() == "No browser app found")
    }

    @Test
    fun readerScreen_showsLinksAsClickable() {
        val content = ArticleContent(
            title = "Article with links",
            paragraphs = listOf("Para"),
            links = listOf(
                com.hopescrolling.data.article.ArticleLink("Reference 1", "https://example.com/ref1"),
                com.hopescrolling.data.article.ArticleLink("Reference 2", "https://example.com/ref2"),
            ),
        )
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(Result.success(content)), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_link_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reader_link_0").assertHasClickAction()
        composeTestRule.onNodeWithTag("reader_link_1").assertIsDisplayed()
    }

    @Test
    fun readerScreen_showsImagesOnSuccess() {
        val content = ArticleContent(
            title = "Article with images",
            paragraphs = listOf("Para"),
            imageUrls = listOf("https://example.com/photo.jpg", "https://example.com/chart.png"),
        )
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(Result.success(content)), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_image_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reader_image_1").assertIsDisplayed()
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
        val viewModel = ArticleReaderViewModel(FakeArticleContentFetcher(result = null), "https://example.com")
        composeTestRule.setContent { ArticleReaderScreen(viewModel = viewModel) }

        composeTestRule.onNodeWithTag("reader_loading").assertIsDisplayed()
    }
}
