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

@Composable
fun FeedManagerScreen(viewModel: FeedManagerViewModel) {
    val feedSources by viewModel.feedSources.collectAsState()
    var addUrl by remember { mutableStateOf("") }
    var renameState by remember { mutableStateOf<Pair<FeedSource, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_manager_screen")
            .padding(16.dp),
    ) {
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
