package com.lhs.sefer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Read-only access to the bundled `nlt.db` (table `nlt`). */
class BibleRepository(private val context: Context) {

    private var db: SQLiteDatabase? = null
    private val database: SQLiteDatabase get() = checkNotNull(db) { "Bible database not opened" }

    suspend fun open() = withContext(Dispatchers.IO) {
        if (db == null) db = openDatabase()
    }

    private fun openDatabase(): SQLiteDatabase {
        val target: File = context.getDatabasePath(DB_NAME)
        val prefs = context.getSharedPreferences("lhs_internal", Context.MODE_PRIVATE)
        if (!target.exists() || prefs.getInt(KEY_DB_VERSION, 0) != DB_VERSION) {
            target.parentFile?.mkdirs()
            context.assets.open(DB_NAME).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            prefs.edit().putInt(KEY_DB_VERSION, DB_VERSION).apply()
        }
        return SQLiteDatabase.openDatabase(target.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    suspend fun chapterCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        val map = HashMap<String, Int>()
        database.rawQuery("SELECT book, MAX(chapternumber) FROM nlt GROUP BY book", null).use { c ->
            while (c.moveToNext()) map[c.getString(0)] = c.getInt(1)
        }
        map
    }

    suspend fun chapter(book: String, chapter: Int): List<Verse> = withContext(Dispatchers.IO) {
        val list = ArrayList<Verse>()
        database.rawQuery(
            "SELECT verseid, versenumber, verse FROM nlt " +
                "WHERE book = ? AND chapternumber = ? ORDER BY versenumber",
            arrayOf(book, chapter.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val text = c.getString(2) ?: ""
                if (text.isBlank()) continue
                list += Verse(c.getInt(0), book, chapter, c.getInt(1), text)
            }
        }
        list
    }

    /** All words / phrases must appear in the verse. [books] limits the search (null = everywhere). */
    suspend fun search(query: String, books: Collection<String>?): SearchResult =
        withContext(Dispatchers.IO) {
            val terms = parseTerms(query)
            if (terms.isEmpty()) return@withContext SearchResult(emptyList(), false)

            val sql = StringBuilder(
                "SELECT verseid, book, chapternumber, versenumber, verse FROM nlt WHERE "
            )
            val args = ArrayList<String>()
            sql.append(terms.joinToString(" AND ") { "verse LIKE ? ESCAPE '\\'" })
            for (t in terms) {
                val escaped = t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                args += "%$escaped%"
            }
            if (books != null) {
                sql.append(" AND book IN (").append(books.joinToString(",") { "?" }).append(")")
                args.addAll(books)
            }
            sql.append(" LIMIT ").append(MAX_RESULTS + 1)

            val found = ArrayList<Verse>()
            database.rawQuery(sql.toString(), args.toTypedArray()).use { c ->
                while (c.moveToNext()) {
                    val text = c.getString(4) ?: ""
                    if (text.isBlank()) continue
                    found += Verse(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), text)
                }
            }
            val truncated = found.size > MAX_RESULTS
            val sorted = found.take(MAX_RESULTS)
                .sortedWith(compareBy({ Books.order(it.book) }, { it.chapter }, { it.number }))
            SearchResult(sorted, truncated)
        }

    private companion object {
        const val DB_NAME = "nlt.db"
        const val DB_VERSION = 1   // bump when the bundled database changes
        const val KEY_DB_VERSION = "db_version"
        const val MAX_RESULTS = 3000
    }
}
