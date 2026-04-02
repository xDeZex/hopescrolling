package com.hopescrolling

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.ui.screens.FeedManagerScreen
import com.hopescrolling.ui.screens.FeedManagerViewModel
import com.hopescrolling.ui.screens.TimelineScreen
import com.hopescrolling.data.rss.Article
import com.hopescrolling.ui.screens.TimelineViewModel
import com.hopescrolling.util.FakeArticleRepository
import com.hopescrolling.util.FakeFeedSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xxhdpi")
@RunWith(RobolectricTestRunner::class)
class ScreenshotTest {

    companion object {
        private val screenshotsDir = File("../screenshots")

        @BeforeClass
        @JvmStatic
        fun clearScreenshots() {
            screenshotsDir.deleteRecursively()
            screenshotsDir.mkdirs()
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun saveScreenshot(name: String) {
        composeTestRule.waitForIdle()
        val decorView = composeTestRule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(
            decorView.width.coerceAtLeast(1),
            decorView.height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        decorView.draw(Canvas(bitmap))
        FileOutputStream(File(screenshotsDir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun screenshot_timelineScreen() {
        val articles = listOf(
            Article(title = "Android 16 Developer Preview Released", link = "https://a.com/1", description = "Google has released the first developer preview of Android 16, featuring new APIs for adaptive layouts.", pubDate = "Tue, 01 Apr 2026 09:00:00 GMT", feedSourceId = "android"),
            Article(title = "Kotlin 2.2 Brings Improved Type Inference", link = "https://a.com/2", description = "The latest Kotlin release ships smarter type inference and faster incremental compilation.", pubDate = "Mon, 31 Mar 2026 14:30:00 GMT", feedSourceId = "kotlin"),
            Article(title = "Jetpack Compose Stability Update", link = "https://a.com/3", description = null, pubDate = "Sun, 30 Mar 2026 08:00:00 GMT", feedSourceId = "android"),
        )
        val viewModel = TimelineViewModel(FakeArticleRepository(articles = articles))
        composeTestRule.setContent { TimelineScreen(viewModel = viewModel) }
        saveScreenshot("timeline_screen")
        assertTrue(File(screenshotsDir, "timeline_screen.png").exists())
    }

    @Test
    fun screenshot_feedManagerScreen_empty() {
        val viewModel = FeedManagerViewModel(FakeFeedSourceRepository())
        composeTestRule.setContent { FeedManagerScreen(viewModel = viewModel) }
        saveScreenshot("feed_manager_empty")
        assertTrue(File(screenshotsDir, "feed_manager_empty.png").exists())
    }

    @Test
    fun screenshot_feedManagerScreen_withFeeds() {
        val repo = FakeFeedSourceRepository()
        repo.sources.value = listOf(
            FeedSource(id = "1", name = "Tech Blog", url = "https://tech.example.com/feed"),
            FeedSource(id = "2", name = "News Feed", url = "https://news.example.com/feed"),
        )
        val viewModel = FeedManagerViewModel(repo)
        composeTestRule.setContent { FeedManagerScreen(viewModel = viewModel) }
        saveScreenshot("feed_manager_with_feeds")
        assertTrue(File(screenshotsDir, "feed_manager_with_feeds.png").exists())
    }
}
