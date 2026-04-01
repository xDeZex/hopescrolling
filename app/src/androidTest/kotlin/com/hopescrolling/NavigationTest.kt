package com.hopescrolling

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timelineScreen_isShownOnLaunch() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("timeline_screen").assertIsDisplayed()
    }

    @Test
    fun manageFeedsButton_navigatesToFeedManager() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("manage_feeds_button").performClick()
        composeTestRule.onNodeWithTag("feed_manager_screen").assertIsDisplayed()
    }

    @Test
    fun backFromFeedManager_returnsToTimeline() {
        composeTestRule.setContent { HopescrollingApp() }
        composeTestRule.onNodeWithTag("manage_feeds_button").performClick()
        composeTestRule.onNodeWithTag("feed_manager_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_button").performClick()
        composeTestRule.onNodeWithTag("timeline_screen").assertIsDisplayed()
    }
}
