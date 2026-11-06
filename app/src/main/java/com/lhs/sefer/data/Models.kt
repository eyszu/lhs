package com.lhs.sefer.data

import androidx.compose.ui.text.font.FontFamily

data class Verse(
    val id: Int,
    val book: String,
    val chapter: Int,
    val number: Int,
    val text: String,
)

data class ReaderLocation(val book: String, val chapter: Int, val verse: Int? = null)

data class SearchResult(val verses: List<Verse>, val truncated: Boolean)

enum class AppTheme(val label: String, val dark: Boolean) {
    LIGHT("Light", false),
    DARK("Dark", true),
    DUSK("Dusk", true),
    COOL("Cool", false),
}

enum class ReaderFont(val label: String, val family: FontFamily) {
    SERIF("Serif", FontFamily.Serif),
    SANS("Sans", FontFamily.SansSerif),
    MONO("Mono", FontFamily.Monospace),
}

/**
 * The database spells God's name in Paleo-Hebrew letters, which many phones have no font for,
 * so the reader can show it in a few other ways.
 */
enum class DivineName(val label: String, val replacement: String?) {
    HEBREW("יהוה", "יהוה"),
    YHWH("YHWH", "YHWH"),
    HASHEM("HaShem", "HaShem"),
    LORD("LORD", "LORD"),
    PALEO("𐤉𐤄𐤅𐤄", null),
}

data class Settings(
    val theme: AppTheme = AppTheme.LIGHT,
    val font: ReaderFont = ReaderFont.SERIF,
    val fontSize: Float = 20f,
    val lineSpacing: Float = 1.6f,
    val divineName: DivineName = DivineName.HEBREW,
)
