package com.hopescrolling.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hopescrolling.data.rss.Article
import com.hopescrolling.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: TimelineViewModel, onOpen: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val showFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val launchBrowser: (String) -> Unit =
        onOpen ?: { url ->
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // No browser available — ignore
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timeline_screen"),
    ) {
        when {
            uiState.isLoading && uiState.articles.isEmpty() -> CenteredFullScreen {
                CircularProgressIndicator(modifier = Modifier.testTag("timeline_loading"))
            }
            uiState.error != null -> CenteredFullScreen {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(Spacing.xl),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.testTag("timeline_error"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("timeline_retry"),
                    ) {
                        Text("Retry")
                    }
                }
            }
            uiState.articles.isEmpty() -> CenteredFullScreen {
                Text(
                    text = "No articles yet.\nAdd feeds to get started.",
                    modifier = Modifier
                        .testTag("timeline_empty")
                        .padding(Spacing.xl),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("timeline_articles"),
                ) {
                    itemsIndexed(uiState.articles) { index, article ->
                        ArticleCard(
                            article = article,
                            index = index,
                            isRead = uiState.readIds.contains(article.link),
                            onRead = { viewModel.markRead(article.link) },
                            onOpen = launchBrowser,
                        )
                    }
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.lg)
                    .testTag("timeline_refresh_fab"),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

@Composable
private fun CenteredFullScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
fun ArticleCard(
    article: Article,
    index: Int,
    isRead: Boolean,
    onRead: () -> Unit,
    onOpen: (String) -> Unit,
) {
    Card(
        onClick = {
            onRead()
            onOpen(article.link)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .semantics { stateDescription = if (isRead) "Read" else "Unread" }
            .testTag("article_card_$index")
            .alpha(if (isRead) 0.5f else 1.0f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.description != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (article.sourceName.isNotEmpty() || article.pubDate != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = listOfNotNull(
                        article.sourceName.takeIf { it.isNotEmpty() },
                        article.pubDate,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
