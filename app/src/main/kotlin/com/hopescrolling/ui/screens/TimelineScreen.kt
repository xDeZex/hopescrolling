package com.hopescrolling.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hopescrolling.data.rss.Article

@Composable
fun TimelineScreen(viewModel: TimelineViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timeline_screen"),
    ) {
        when {
            uiState.isLoading -> CenteredFullScreen {
                CircularProgressIndicator(modifier = Modifier.testTag("timeline_loading"))
            }
            uiState.error != null -> CenteredFullScreen {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.error!!, modifier = Modifier.testTag("timeline_error"))
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
                    text = "No articles yet. Add feeds to get started.",
                    modifier = Modifier.testTag("timeline_empty"),
                )
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(uiState.articles) { index, article ->
                    ArticleCard(
                        article = article,
                        index = index,
                        isRead = uiState.readIds.contains(article.link),
                        onRead = { viewModel.markRead(article.link) },
                    )
                }
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
fun ArticleCard(article: Article, index: Int, isRead: Boolean, onRead: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = {
            onRead()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // No browser available — ignore
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("article_card_$index")
            .alpha(if (isRead) 0.5f else 1.0f),
    ) {
        Text(
            text = article.title,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        if (article.description != null) {
            Text(
                text = article.description,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        if (article.sourceName.isNotEmpty() || article.pubDate != null) {
            Text(
                text = listOfNotNull(article.sourceName.takeIf { it.isNotEmpty() }, article.pubDate).joinToString(" · "),
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
            )
        }
    }
}
