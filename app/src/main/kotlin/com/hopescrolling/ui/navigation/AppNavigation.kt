package com.hopescrolling.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hopescrolling.AppContainer
import com.hopescrolling.ui.screens.FeedManagerScreen
import com.hopescrolling.ui.screens.FeedManagerViewModel
import com.hopescrolling.ui.screens.TimelineScreen
import com.hopescrolling.ui.screens.TimelineViewModel

private const val ROUTE_TIMELINE = "timeline"
private const val ROUTE_FEED_MANAGER = "feed_manager"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    if (currentRoute == ROUTE_FEED_MANAGER) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.testTag("back_button"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute == ROUTE_TIMELINE) {
                        IconButton(
                            onClick = { navController.navigate(ROUTE_FEED_MANAGER) },
                            modifier = Modifier.testTag("manage_feeds_button"),
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Manage feeds")
                        }
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_TIMELINE,
            modifier = Modifier.padding(padding),
        ) {
            composable(ROUTE_TIMELINE) {
                val viewModel = viewModel { TimelineViewModel(container.articleRepository, container.readStateRepository) }
                TimelineScreen(viewModel)
            }
            composable(ROUTE_FEED_MANAGER) {
                val viewModel = viewModel { FeedManagerViewModel(container.feedSourceRepository) }
                FeedManagerScreen(viewModel)
            }
        }
    }
}
