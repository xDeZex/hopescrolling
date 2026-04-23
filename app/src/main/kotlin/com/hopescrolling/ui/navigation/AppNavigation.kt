package com.hopescrolling.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hopescrolling.AppContainer
import com.hopescrolling.ui.screens.ArticleReaderScreen
import com.hopescrolling.ui.screens.ArticleReaderViewModel
import com.hopescrolling.ui.screens.SettingsScreen
import com.hopescrolling.ui.screens.SettingsViewModel
import com.hopescrolling.ui.screens.TimelineScreen
import com.hopescrolling.ui.screens.TimelineViewModel

private const val ROUTE_TIMELINE = "timeline"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_READER = "reader/{encodedUrl}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val readerUrl = if (currentRoute?.startsWith("reader/") == true) {
        backStackEntry?.arguments?.getString("encodedUrl")?.let { Uri.decode(it) }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    if (currentRoute == ROUTE_SETTINGS || currentRoute?.startsWith("reader/") == true) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.testTag("back_button"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (readerUrl != null) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(readerUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    context.startActivity(intent)
                                } catch (_: ActivityNotFoundException) {}
                            },
                            modifier = Modifier.testTag("open_in_browser_button"),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Open in browser")
                        }
                    }
                    if (currentRoute == ROUTE_TIMELINE) {
                        IconButton(
                            onClick = { navController.navigate(ROUTE_SETTINGS) },
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
                TimelineScreen(viewModel, onOpen = { url ->
                    navController.navigate("reader/${Uri.encode(url)}")
                })
            }
            composable(ROUTE_SETTINGS) {
                val viewModel = viewModel { SettingsViewModel(container.feedSourceRepository, container.appUpdateRepository) }
                SettingsScreen(viewModel)
            }
            composable(ROUTE_READER) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: return@composable
                val url = Uri.decode(encodedUrl)
                val viewModel = viewModel { ArticleReaderViewModel(container.articleContentFetcher, url) }
                ArticleReaderScreen(viewModel)
            }
        }
    }
}
