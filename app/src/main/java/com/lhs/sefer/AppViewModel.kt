package com.lhs.sefer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lhs.sefer.data.AppTheme
import com.lhs.sefer.data.BibleRepository
import com.lhs.sefer.data.BookmarkStore
import com.lhs.sefer.data.Books
import com.lhs.sefer.data.DivineName
import com.lhs.sefer.data.ReaderFont
import com.lhs.sefer.data.ReaderLocation
import com.lhs.sefer.data.Settings
import com.lhs.sefer.data.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class Tab { READ, SEARCH, BOOKMARKS, SETTINGS }

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("lhs_prefs", Context.MODE_PRIVATE)
    private val store = BookmarkStore(app)
    val repo = BibleRepository(app)

    var ready by mutableStateOf(false)
        private set
    var chapterCounts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    var settings by mutableStateOf(loadSettings())
        private set

    var bookmarks by mutableStateOf<List<Verse>>(emptyList())
        private set
    var bookmarkedIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    /** Verse id -> the user's own wording. The bundled database itself is never modified. */
    var verseEdits by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

    var tab by mutableStateOf(Tab.READ)
    var reader by mutableStateOf<ReaderLocation?>(null)
        private set
    var lastRead by mutableStateOf(loadLastRead())
        private set

    init {
        viewModelScope.launch {
            repo.open()
            chapterCounts = repo.chapterCounts()
            replaceBookmarks(kotlinx.coroutines.withContext(Dispatchers.IO) { store.all() })
            verseEdits = kotlinx.coroutines.withContext(Dispatchers.IO) { store.edits() }
            ready = true
        }
    }

    // ---------- verse editing ----------

    /** The verse with the user's edit applied (if any). */
    fun withEdit(v: Verse): Verse = verseEdits[v.id]?.let { v.copy(text = it) } ?: v

    fun isEdited(v: Verse): Boolean = v.id in verseEdits

    fun saveVerseEdit(v: Verse, newText: String) {
        val text = newText.trim()
        if (text.isEmpty()) return
        verseEdits = verseEdits + (v.id to text)
        viewModelScope.launch(Dispatchers.IO) { store.saveEdit(v.id, text) }
    }

    fun revertVerseEdit(v: Verse) {
        verseEdits = verseEdits - v.id
        viewModelScope.launch(Dispatchers.IO) { store.deleteEdit(v.id) }
    }

    // ---------- settings ----------

    private fun loadSettings(): Settings {
        val systemDark = (getApplication<Application>().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return Settings(
            theme = prefs.getString("theme", null)?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: if (systemDark) AppTheme.DARK else AppTheme.LIGHT,
            font = prefs.getString("font", null)?.let { runCatching { ReaderFont.valueOf(it) }.getOrNull() }
                ?: ReaderFont.SERIF,
            fontSize = prefs.getFloat("font_size", 20f),
            lineSpacing = prefs.getFloat("line_spacing", 1.6f),
            divineName = prefs.getString("divine", null)?.let { runCatching { DivineName.valueOf(it) }.getOrNull() }
                ?: DivineName.HEBREW,
        )
    }

    fun setTheme(t: AppTheme) {
        settings = settings.copy(theme = t)
        prefs.edit().putString("theme", t.name).apply()
    }

    fun setFont(f: ReaderFont) {
        settings = settings.copy(font = f)
        prefs.edit().putString("font", f.name).apply()
    }

    fun setFontSize(size: Float) {
        val s = size.roundToInt().coerceIn(14, 34).toFloat()
        settings = settings.copy(fontSize = s)
        prefs.edit().putFloat("font_size", s).apply()
    }

    fun setLineSpacing(v: Float) {
        val s = ((v * 10).roundToInt() / 10f).coerceIn(1.2f, 2.2f)
        settings = settings.copy(lineSpacing = s)
        prefs.edit().putFloat("line_spacing", s).apply()
    }

    fun setDivineName(d: DivineName) {
        settings = settings.copy(divineName = d)
        prefs.edit().putString("divine", d.name).apply()
    }

    // ---------- reading ----------

    private fun loadLastRead(): ReaderLocation? {
        val book = prefs.getString("last_book", null) ?: return null
        if (Books.getOrNull(book) == null) return null
        return ReaderLocation(book, prefs.getInt("last_chapter", 1))
    }

    fun openChapter(book: String, chapter: Int, verse: Int? = null) {
        reader = ReaderLocation(book, chapter, verse)
        lastRead = ReaderLocation(book, chapter)
        prefs.edit().putString("last_book", book).putInt("last_chapter", chapter).apply()
    }

    fun closeReader() {
        reader = null
    }

    /** Previous (-1) / next (+1) chapter, crossing book boundaries. */
    fun adjacent(loc: ReaderLocation, delta: Int): ReaderLocation? {
        val idx = Books.all.indexOfFirst { it.code == loc.book }
        if (idx < 0) return null
        return if (delta > 0) {
            if (loc.chapter < (chapterCounts[loc.book] ?: 0)) {
                ReaderLocation(loc.book, loc.chapter + 1)
            } else {
                Books.all.getOrNull(idx + 1)?.let { ReaderLocation(it.code, 1) }
            }
        } else {
            if (loc.chapter > 1) {
                ReaderLocation(loc.book, loc.chapter - 1)
            } else {
                Books.all.getOrNull(idx - 1)?.let { ReaderLocation(it.code, chapterCounts[it.code] ?: 1) }
            }
        }
    }

    // ---------- bookmarks ----------

    private fun replaceBookmarks(list: List<Verse>) {
        bookmarks = list
        bookmarkedIds = list.mapTo(HashSet()) { it.id }
    }

    /** Adds the verses; if they are all bookmarked already, removes them. Returns true when added. */
    fun toggleBookmarks(verses: List<Verse>): Boolean {
        val marked = bookmarkedIds
        return if (verses.isNotEmpty() && verses.all { it.id in marked }) {
            removeBookmarks(verses)
            false
        } else {
            addBookmarks(verses.filter { it.id !in marked })
            true
        }
    }

    fun addBookmarks(verses: List<Verse>) {
        if (verses.isEmpty()) return
        replaceBookmarks(verses.reversed() + bookmarks.filter { b -> verses.none { it.id == b.id } })
        viewModelScope.launch(Dispatchers.IO) { store.add(verses) }
    }

    fun removeBookmarks(verses: List<Verse>) {
        val ids = verses.map { it.id }.toSet()
        replaceBookmarks(bookmarks.filter { it.id !in ids })
        viewModelScope.launch(Dispatchers.IO) { store.remove(ids) }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}
