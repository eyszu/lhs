package com.lhs.sefer.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

const val PALEO_YHWH = "𐤉𐤄𐤅𐤄"

private val footnoteRegex = Regex("""\[[a-z]]""")

/** Text exactly as it should be shown (and copied) given the reader's settings. */
fun displayText(raw: String, divine: DivineName): String {
    var t = raw
    if (t.indexOf('\t') >= 0) t = t.replace("\t", " — ")
    t = footnoteRegex.replace(t, "")
    divine.replacement?.let { t = t.replace(PALEO_YHWH, it) }
    return t
}

private fun run(a: Int, b: Int) = if (a == b) "$a" else "$a–$b"

private val canonical = compareBy<Verse>({ Books.order(it.book) }, { it.chapter }, { it.number })

/** "Genesis 1:1–3, 5" / "Psalm 23:1; Isaiah 53:5" */
fun formatReference(verses: List<Verse>): String {
    val sorted = verses.sortedWith(canonical)
    return sorted.groupBy { it.book to it.chapter }.entries.joinToString("; ") { (key, vs) ->
        val nums = vs.map { it.number }.distinct()
        val runs = mutableListOf<String>()
        var start = nums[0]
        var prev = nums[0]
        for (n in nums.drop(1)) {
            if (n == prev + 1) {
                prev = n
            } else {
                runs += run(start, prev)
                start = n
                prev = n
            }
        }
        runs += run(start, prev)
        "${Books.get(key.first).name} ${key.second}:${runs.joinToString(", ")}"
    }
}

/** Text placed on the clipboard / shared. */
fun formatForCopy(verses: List<Verse>, divine: DivineName): String {
    val sorted = verses.sortedWith(canonical)
    val ref = formatReference(sorted)
    return if (sorted.size == 1) {
        "“${displayText(sorted[0].text, divine)}”\n— $ref (NLT)"
    } else {
        sorted.joinToString("\n") { "${it.number} ${displayText(it.text, divine)}" } + "\n— $ref (NLT)"
    }
}

fun Context.copyToClipboard(label: String, text: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, null))
}

/**
 * Splits a search box entry into terms. Words are AND-ed; "quoted text" is kept as one phrase.
 * Typing yhwh / yahweh / יהוה finds the divine name.
 */
fun parseTerms(query: String): List<String> {
    val out = mutableListOf<String>()
    Regex("\"([^\"]+)\"|(\\S+)").findAll(query).forEach { m ->
        val t = (m.groups[1]?.value ?: m.groups[2]?.value ?: "").trim()
        if (t.isNotEmpty()) {
            val lower = t.lowercase()
            out += if (lower == "yhwh" || lower == "yahweh" || t == "יהוה") PALEO_YHWH else t
        }
    }
    return out
}

fun highlight(text: String, terms: List<String>, background: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        val lower = text.lowercase()
        if (lower.length != text.length) return@buildAnnotatedString
        for (t in terms) {
            val tl = t.lowercase()
            var i = lower.indexOf(tl)
            while (i >= 0) {
                addStyle(SpanStyle(background = background, fontWeight = FontWeight.Bold), i, i + tl.length)
                i = lower.indexOf(tl, i + tl.length)
            }
        }
    }
