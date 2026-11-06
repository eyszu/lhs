package com.lhs.sefer.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Bookmarks live in their own small database so they survive Bible-database updates. */
class BookmarkStore(context: Context) : SQLiteOpenHelper(context, "user.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        createEditsTable(db)
        db.execSQL(
            "CREATE TABLE bookmarks (" +
                "verseid INTEGER PRIMARY KEY, " +
                "book TEXT NOT NULL, " +
                "chapter INTEGER NOT NULL, " +
                "number INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "created INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createEditsTable(db)
    }

    private fun createEditsTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS verse_edits (verseid INTEGER PRIMARY KEY, text TEXT NOT NULL)")
    }

    fun edits(): Map<Int, String> {
        val map = HashMap<Int, String>()
        readableDatabase.rawQuery("SELECT verseid, text FROM verse_edits", null).use { c ->
            while (c.moveToNext()) map[c.getInt(0)] = c.getString(1)
        }
        return map
    }

    fun saveEdit(verseId: Int, text: String) {
        val values = ContentValues().apply {
            put("verseid", verseId)
            put("text", text)
        }
        writableDatabase.insertWithOnConflict("verse_edits", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteEdit(verseId: Int) {
        writableDatabase.delete("verse_edits", "verseid = ?", arrayOf(verseId.toString()))
    }

    /** Newest first. */
    fun all(): List<Verse> {
        val list = ArrayList<Verse>()
        readableDatabase.rawQuery(
            "SELECT verseid, book, chapter, number, text FROM bookmarks ORDER BY created DESC, verseid DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list += Verse(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getString(4))
            }
        }
        return list
    }

    fun add(verses: List<Verse>) {
        val db = writableDatabase
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            verses.forEachIndexed { i, v ->
                val values = ContentValues().apply {
                    put("verseid", v.id)
                    put("book", v.book)
                    put("chapter", v.chapter)
                    put("number", v.number)
                    put("text", v.text)
                    put("created", now + i)
                }
                db.insertWithOnConflict("bookmarks", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun remove(ids: Collection<Int>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            ids.forEach { db.delete("bookmarks", "verseid = ?", arrayOf(it.toString())) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
