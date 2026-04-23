package com.hopescrolling.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.data.update.UpdateState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onDownloadUpdate: (String) -> Unit = {},
) {
    val feedSources by viewModel.feedSources.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    var addUrl by remember { mutableStateOf("") }
    var renameState by remember { mutableStateOf<Pair<FeedSource, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(16.dp),
    ) {
        AppUpdateSection(
            state = updateState,
            isDownloading = isDownloading,
            onDownload = { apkUrl ->
                viewModel.startDownload()
                onDownloadUpdate(apkUrl)
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = addUrl,
                onValueChange = { addUrl = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_feed_input"),
                placeholder = { Text("Feed URL") },
            )
            Button(
                onClick = {
                    if (addUrl.isNotBlank()) {
                        viewModel.addFeed(addUrl)
                        addUrl = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("add_feed_button"),
            ) {
                Text("Add")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(feedSources, key = { it.id }) { source ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("feed_item_${source.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = source.name,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { renameState = source to source.name },
                        modifier = Modifier.testTag("rename_feed_${source.id}"),
                    ) {
                        Text("R")
                    }
                    IconButton(
                        onClick = { viewModel.deleteFeed(source.id) },
                        modifier = Modifier.testTag("delete_feed_${source.id}"),
                    ) {
                        Text("X")
                    }
                }
            }
        }
    }

    renameState?.let { (source, input) ->
        AlertDialog(
            onDismissRequest = { renameState = null },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { renameState = source to it },
                    modifier = Modifier.testTag("rename_dialog_input"),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            viewModel.renameFeed(source.id, input)
                            renameState = null
                        }
                    },
                    modifier = Modifier.testTag("rename_dialog_confirm"),
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameState = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AppUpdateSection(
    state: UpdateState,
    isDownloading: Boolean = false,
    onDownload: (String) -> Unit = {},
) {
    when (state) {
        UpdateState.Loading -> CircularProgressIndicator(modifier = Modifier.testTag("update_loading"))
        UpdateState.UpToDate -> Text(
            text = "Up to date",
            modifier = Modifier.testTag("update_up_to_date"),
        )
        is UpdateState.UpdateAvailable -> Column {
            Text(
                text = "Installed: build-${com.hopescrolling.BuildConfig.VERSION_CODE}",
                modifier = Modifier.testTag("update_installed_version"),
            )
            Text(
                text = "Latest: ${state.latestLabel}",
                modifier = Modifier.testTag("update_latest_version"),
            )
            Button(
                onClick = { onDownload(state.apkUrl) },
                enabled = !isDownloading,
                modifier = Modifier.testTag("update_download_button"),
            ) {
                Text(if (isDownloading) "Downloading..." else "Download Update")
            }
        }
        UpdateState.Error -> Text(
            text = "Could not check for updates",
            modifier = Modifier.testTag("update_error"),
        )
    }
}
