@file:OptIn(ExperimentalMaterial3Api::class)

package com.lhs.sefer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lhs.sefer.AppViewModel
import com.lhs.sefer.data.BookGroup
import com.lhs.sefer.data.Books
import com.lhs.sefer.data.SearchResult
import com.lhs.sefer.data.Verse
import com.lhs.sefer.data.copyToClipboard
import com.lhs.sefer.data.displayText
import com.lhs.sefer.data.formatForCopy
import com.lhs.sefer.data.formatReference
import com.lhs.sefer.data.highlight
import com.lhs.sefer.data.parseTerms
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchTab(vm: AppViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val settings = vm.settings

    var query by rememberSaveable { mutableStateOf("") }
    var scopeIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = whole Tanakh
    var result by remember { mutableStateOf<SearchResult?>(null) }
    var loading by remember { mutableStateOf(false) }

    val reference = remember(query, vm.chapterCounts) { Books.parseReference(query, vm.chapterCounts) }
    val terms = remember(query) { parseTerms(query) }

    LaunchedEffect(query, scopeIndex) {
        if (query.isBlank()) {
            result = null
            loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(300) // debounce typing; cancelled automatically when the query changes
        val books = if (scopeIndex == 0) null
        else Books.byGroup.getValue(BookGroup.entries[scopeIndex - 1]).map { it.code }
        result = vm.repo.search(query, books)
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            singleLine = true,
            placeholder = { Text("Search words, or a verse like Psalm 23") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        )

        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val labels = listOf("All") + BookGroup.entries.map { it.title.substringBefore(" ·") }
            labels.forEachIndexed { i, label ->
                FilterChip(selected = scopeIndex == i, onClick = { scopeIndex = i }, label = { Text(label) })
            }
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
            if (reference != null) {
                item("goto") {
                    val b = Books.get(reference.book)
                    val label = buildString {
                        append("${b.name} ${reference.chapter}")
                        if (reference.verse != null) append(":${reference.verse}")
                    }
                    Card(
                        onClick = { vm.openChapter(reference.book, reference.chapter, reference.verse) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Go to", style = MaterialTheme.typography.labelLarge)
                                Text(label, style = MaterialTheme.typography.titleLarge)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            if (query.isBlank()) {
                item("hint") {
                    Text(
                        "Search the whole Tanakh by word or phrase.\n\n" +
                            "• Several words must all appear in the verse.\n" +
                            "• Use \"quotes\" for an exact phrase.\n" +
                            "• Type a reference such as Isa 53 or 1 Sam 17:45 to jump straight to it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            } else if (loading && result == null) {
                item("loading") {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val r = result
                if (r != null) {
                    item("count") {
                        val n = r.verses.size
                        val text = when {
                            n == 0 -> "No verses found"
                            r.truncated -> "Showing the first $n verses — try a more specific search"
                            n == 1 -> "1 verse"
                            else -> "$n verses"
                        }
                        Text(
                            text,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }
                    items(r.verses, key = { it.id }) { original ->
                        val verse = vm.withEdit(original)
                        ResultRow(
                            verse = verse,
                            terms = terms,
                            bookmarked = verse.id in vm.bookmarkedIds,
                            vm = vm,
                            onOpen = { vm.openChapter(verse.book, verse.chapter, verse.number) },
                            onCopy = {
                                context.copyToClipboard("Verse", formatForCopy(listOf(verse), settings.divineName))
                                scope.launch { snackbar.showSnackbar("Verse copied") }
                            },
                            onBookmark = {
                                val added = vm.toggleBookmarks(listOf(verse))
                                scope.launch { snackbar.showSnackbar(if (added) "Bookmarked" else "Bookmark removed") }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    verse: Verse,
    terms: List<String>,
    bookmarked: Boolean,
    vm: AppViewModel,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onBookmark: () -> Unit,
) {
    val shown = displayText(verse.text, vm.settings.divineName)
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val body = remember(shown, terms, highlightColor) { highlight(shown, terms, highlightColor) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(start = 20.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    formatReference(listOf(verse)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 2.dp))
            }
            Column {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy verse", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (bookmarked) "Remove bookmark" else "Bookmark verse",
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
