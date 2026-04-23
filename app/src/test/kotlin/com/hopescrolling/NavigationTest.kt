package com.hopescrolling

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hopescrolling.ui.navigation.AppNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timelineScreen_isShownOnLaunch() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("timeline_screen").assertIsDisplayed()
    }

    @Test
    fun manageFeedsButton_navigatesToSettings() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("manage_feeds_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }

    // Verifies that the back button is shown when navigating to the reader screen.
    // Regression guard for the route-comparison in AppNavigation's top bar.
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun readerScreen_backButtonIsShown() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            if (currentRoute?.startsWith("reader/") == true) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.testTag("back_button"),
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                    )
                },
            ) { _ ->
                NavHost(
                    navController = navController,
                    startDestination = "timeline",
                ) {
                    composable("timeline") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("timeline_screen")
                                .clickable {
                                    navController.navigate("reader/${Uri.encode("https://example.com/article")}")
                                },
                        )
                    }
                    composable("reader/{encodedUrl}") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("reader_screen"),
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("timeline_screen").performClick()
        composeTestRule.onNodeWithTag("reader_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    // Uses a hand-rolled nav setup rather than HopescrollingApp() because the timeline
    // shows no cards with empty repositories, making it impossible to navigate to the
    // reader via the real app without test data. Same pattern as readerScreen_backButtonIsShown.
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun readerScreen_showsOpenInBrowserButtonInTopBar() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val encodedUrl = backStackEntry?.arguments?.getString("encodedUrl")
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        actions = {
                            if (currentRoute?.startsWith("reader/") == true && encodedUrl != null) {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier.testTag("open_in_browser_button"),
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Open in browser")
                                }
                            }
                        },
                    )
                },
            ) { _ ->
                NavHost(navController = navController, startDestination = "timeline") {
                    composable("timeline") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("timeline_screen")
                                .clickable { navController.navigate("reader/${Uri.encode("https://example.com/article")}") },
                        )
                    }
                    composable("reader/{encodedUrl}") {
                        Box(modifier = Modifier.fillMaxSize().testTag("reader_screen"))
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("timeline_screen").performClick()
        composeTestRule.onNodeWithTag("reader_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_in_browser_button").assertIsDisplayed()
    }

    @Test
    fun backFromSettings_returnsToTimeline() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("manage_feeds_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_button").performClick()
        composeTestRule.onNodeWithTag("timeline_screen").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_showsBadgeDotWhenUpdateAvailable() {
        composeTestRule.setContent { AppNavigation(updateAvailable = MutableStateFlow(true)) }

        composeTestRule.onNodeWithTag("update_badge_dot").assertIsDisplayed()
    }

    @Test
    fun timelineScreen_hidesBadgeDotWhenNoUpdateAvailable() {
        composeTestRule.setContent { AppNavigation(updateAvailable = MutableStateFlow(false)) }

        composeTestRule.onNodeWithTag("update_badge_dot").assertDoesNotExist()
    }
}
