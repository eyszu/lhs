package com.lhs.sefer.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Bookmarks live in their own small database so they survive Bible-database updates. */
class BookmarkStore(context: Context) : SQLiteOpenHelper(context, "user.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

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
