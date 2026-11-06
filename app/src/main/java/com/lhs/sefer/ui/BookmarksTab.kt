@file:OptIn(ExperimentalMaterial3Api::class)

package com.lhs.sefer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lhs.sefer.AppViewModel
import com.lhs.sefer.data.copyToClipboard
import com.lhs.sefer.data.displayText
import com.lhs.sefer.data.formatForCopy
import com.lhs.sefer.data.formatReference
import kotlinx.coroutines.launch

@Composable
fun BookmarksTab(vm: AppViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val divine = vm.settings.divineName
    val list = vm.bookmarks

    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    "No bookmarks yet",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "Tap a verse while reading, then tap the bookmark icon to save it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        items(list, key = { it.id }) { verse ->
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.openChapter(verse.book, verse.chapter, verse.number) }
                        .padding(start = 20.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            formatReference(listOf(verse)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            displayText(verse.text, divine),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Column {
                        IconButton(onClick = {
                            context.copyToClipboard("Verse", formatForCopy(listOf(verse), divine))
                            scope.launch { snackbar.showSnackbar("Verse copied") }
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy verse", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = {
                            vm.removeBookmarks(listOf(verse))
                            scope.launch {
                                val r = snackbar.showSnackbar("Bookmark removed", actionLabel = "Undo")
                                if (r == SnackbarResult.ActionPerformed) vm.addBookmarks(listOf(verse))
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark", modifier = Modifier.size(20.dp))
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
