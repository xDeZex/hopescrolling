package com.hopescrolling

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.ui.screens.FeedManagerScreen
import com.hopescrolling.ui.screens.FeedManagerViewModel
import com.hopescrolling.ui.screens.TimelineScreen
import com.hopescrolling.util.FakeFeedSourceRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertTrue
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

    private fun viewModelScope() = TestScope(UnconfinedTestDispatcher())

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
        composeTestRule.setContent { TimelineScreen() }
        saveScreenshot("timeline_screen")
        assertTrue(File(screenshotsDir, "timeline_screen.png").exists())
    }

    @Test
    fun screenshot_feedManagerScreen_empty() {
        val viewModel = FeedManagerViewModel(FakeFeedSourceRepository(), viewModelScope())
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
        val viewModel = FeedManagerViewModel(repo, viewModelScope())
        composeTestRule.setContent { FeedManagerScreen(viewModel = viewModel) }
        saveScreenshot("feed_manager_with_feeds")
        assertTrue(File(screenshotsDir, "feed_manager_with_feeds.png").exists())
    }
}
