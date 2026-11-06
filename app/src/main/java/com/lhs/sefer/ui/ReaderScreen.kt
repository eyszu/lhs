@file:OptIn(ExperimentalMaterial3Api::class)

package com.lhs.sefer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lhs.sefer.AppViewModel
import com.lhs.sefer.data.Books
import com.lhs.sefer.data.ReaderLocation
import com.lhs.sefer.data.Settings
import com.lhs.sefer.data.Verse
import com.lhs.sefer.data.copyToClipboard
import com.lhs.sefer.data.displayText
import com.lhs.sefer.data.formatForCopy
import com.lhs.sefer.data.shareText
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(vm: AppViewModel, loc: ReaderLocation, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val book = Books.get(loc.book)
    val settings = vm.settings

    var verses by remember(loc.book, loc.chapter) { mutableStateOf<List<Verse>?>(null) }
    var selected by remember(loc.book, loc.chapter) { mutableStateOf<Set<Int>>(emptySet()) }
    var showSheet by remember { mutableStateOf(false) }
    val listState = remember(loc.book, loc.chapter) { LazyListState() }

    LaunchedEffect(loc.book, loc.chapter) {
        verses = null
        val loaded = vm.repo.chapter(loc.book, loc.chapter)
        verses = loaded
        val target = loc.verse
        if (target != null) {
            val index = loaded.indexOfFirst { it.number == target }
            if (index >= 0) listState.scrollToItem(index + 1) // item 0 is the chapter heading
        }
    }

    BackHandler {
        if (selected.isNotEmpty()) selected = emptySet() else vm.closeReader()
    }

    val chosenOriginal: List<Verse> = verses.orEmpty().filter { it.id in selected }
    val chosen: List<Verse> = chosenOriginal.map { vm.withEdit(it) }
    var editing by remember { mutableStateOf<Verse?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("${book.name} ${loc.chapter}") },
                navigationIcon = {
                    IconButton(onClick = { vm.closeReader() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Filled.TextFields, contentDescription = "Font and theme")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (chosen.isNotEmpty()) {
                SelectionBar(
                    count = chosen.size,
                    allBookmarked = chosen.all { it.id in vm.bookmarkedIds },
                    onClear = { selected = emptySet() },
                    onCopy = {
                        context.copyToClipboard("Verse", formatForCopy(chosen, settings.divineName))
                        scope.launch { snackbar.showSnackbar(if (chosen.size == 1) "Verse copied" else "${chosen.size} verses copied") }
                        selected = emptySet()
                    },
                    onBookmark = {
                        val added = vm.toggleBookmarks(chosen)
                        scope.launch { snackbar.showSnackbar(if (added) "Bookmarked" else "Bookmark removed") }
                        selected = emptySet()
                    },
                    onShare = {
                        context.shareText(formatForCopy(chosen, settings.divineName))
                    },
                    // editing is only offered when exactly one verse is selected
                    onEdit = if (chosenOriginal.size == 1) {
                        { editing = chosenOriginal[0] }
                    } else null,
                )
            } else {
                ChapterBar(
                    label = "${book.name} ${loc.chapter}",
                    hasPrev = vm.adjacent(loc, -1) != null,
                    hasNext = vm.adjacent(loc, +1) != null,
                    onPrev = { vm.adjacent(loc, -1)?.let { vm.openChapter(it.book, it.chapter) } },
                    onNext = { vm.adjacent(loc, +1)?.let { vm.openChapter(it.book, it.chapter) } },
                )
            }
        },
    ) { padding ->
        val list = verses
        if (list == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                item("heading") {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            book.hebrew,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "${book.name} ${loc.chapter}",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(list, key = { it.id }) { verse ->
                    VerseRow(
                        verse = vm.withEdit(verse),
                        settings = settings,
                        selected = verse.id in selected,
                        bookmarked = verse.id in vm.bookmarkedIds,
                        highlighted = loc.verse == verse.number,
                        onClick = {
                            selected = if (verse.id in selected) selected - verse.id else selected + verse.id
                        },
                    )
                }
                item("footer") {
                  /*  Text(
                        "Tap verses to select them, then copy, bookmark or share.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    ) */
                }
            }
        }
    }

    editing?.let { original ->
        EditVerseDialog(
            reference = "${book.name} ${original.chapter}:${original.number}",
            initialText = displayText(vm.withEdit(original).text, settings.divineName),
            isEdited = vm.isEdited(original),
            onSave = { newText ->
                vm.saveVerseEdit(original, newText)
                editing = null
                selected = emptySet()
                scope.launch { snackbar.showSnackbar("Verse updated") }
            },
            onRevert = {
                vm.revertVerseEdit(original)
                editing = null
                selected = emptySet()
                scope.launch { snackbar.showSnackbar("Original text restored") }
            },
            onDismiss = { editing = null },
        )
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Text("Theme", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 10.dp))
                ThemePicker(settings.theme, vm::setTheme)
                SectionTitle("Text")
                ReadingControls(settings, vm)
                Spacer(Modifier.height(24.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun VerseRow(
    verse: Verse,
    settings: Settings,
    selected: Boolean,
    bookmarked: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val background = when {
        selected -> cs.primaryContainer
        highlighted -> cs.secondaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        selected -> cs.onPrimaryContainer
        highlighted -> cs.onSecondaryContainer
        else -> cs.onBackground
    }
    val numberColor = if (selected) cs.onPrimaryContainer else cs.primary
    val text = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = numberColor,
                fontWeight = FontWeight.Bold,
                fontSize = (settings.fontSize * 0.62f).sp,
                baselineShift = BaselineShift.Superscript,
            )
        ) { append("${verse.number} ") }
        append(displayText(verse.text, settings.divineName))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = settings.font.family,
                fontSize = settings.fontSize.sp,
                lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                color = textColor,
            ),
        )
        if (bookmarked) {
            Icon(
                Icons.Filled.Bookmark,
                contentDescription = "Bookmarked",
                tint = numberColor,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp).size(18.dp),
            )
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    allBookmarked: Boolean,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onEdit: (() -> Unit)?,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClear) { Icon(Icons.Filled.Close, contentDescription = "Clear selection") }
            Text(
                if (count == 1) "1 verse" else "$count verses",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (onEdit != null) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit verse") }
            }
            IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy") }
            IconButton(onClick = onBookmark) {
                Icon(
                    if (allBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (allBookmarked) "Remove bookmark" else "Bookmark",
                )
            }
            IconButton(onClick = onShare) { Icon(Icons.Filled.Share, contentDescription = "Share") }
        }
    }
}

@Composable
private fun ChapterBar(
    label: String,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrev, enabled = hasPrev) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous chapter")
            }
            Text(label, style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onNext, enabled = hasNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next chapter")
            }
        }
    }
}

@Composable
private fun EditVerseDialog(
    reference: String,
    initialText: String,
    isEdited: Boolean,
    onSave: (String) -> Unit,
    onRevert: () -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $reference") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 10,
                )
                if (isEdited) {
                    TextButton(onClick = onRevert, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Restore original text")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
