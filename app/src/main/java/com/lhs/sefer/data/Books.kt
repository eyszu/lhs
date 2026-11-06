package com.lhs.sefer.data

enum class BookGroup(val title: String, val hebrew: String) {
    TORAH("Torah", "תורה"),
    NEVIIM("Nevi'im · Prophets", "נביאים"),
    KETUVIM("Ketuvim · Writings", "כתובים"),
}

data class BookInfo(
    val code: String,      // code used in the database ("Gen", "1Sa", ...)
    val name: String,      // English name
    val translit: String,  // Hebrew name, transliterated
    val hebrew: String,    // Hebrew name, in Hebrew letters
    val group: BookGroup,
)

data class Reference(val book: String, val chapter: Int, val verse: Int?)

/** The 39 books in Tanakh order (Torah, Nevi'im, Ketuvim). */
object Books {
    val all: List<BookInfo> = listOf(
        // Torah
        BookInfo("Gen", "Genesis", "Bereshit", "בראשית", BookGroup.TORAH),
        BookInfo("Exo", "Exodus", "Shemot", "שמות", BookGroup.TORAH),
        BookInfo("Lev", "Leviticus", "Vayikra", "ויקרא", BookGroup.TORAH),
        BookInfo("Num", "Numbers", "Bamidbar", "במדבר", BookGroup.TORAH),
        BookInfo("Deu", "Deuteronomy", "Devarim", "דברים", BookGroup.TORAH),
        // Nevi'im
        BookInfo("Jos", "Joshua", "Yehoshua", "יהושע", BookGroup.NEVIIM),
        BookInfo("Jdg", "Judges", "Shoftim", "שופטים", BookGroup.NEVIIM),
        BookInfo("1Sa", "1 Samuel", "Shmuel Alef", "שמואל א", BookGroup.NEVIIM),
        BookInfo("2Sa", "2 Samuel", "Shmuel Bet", "שמואל ב", BookGroup.NEVIIM),
        BookInfo("1Ki", "1 Kings", "Melachim Alef", "מלכים א", BookGroup.NEVIIM),
        BookInfo("2Ki", "2 Kings", "Melachim Bet", "מלכים ב", BookGroup.NEVIIM),
        BookInfo("Isa", "Isaiah", "Yeshayahu", "ישעיהו", BookGroup.NEVIIM),
        BookInfo("Jer", "Jeremiah", "Yirmeyahu", "ירמיהו", BookGroup.NEVIIM),
        BookInfo("Eze", "Ezekiel", "Yechezkel", "יחזקאל", BookGroup.NEVIIM),
        BookInfo("Hos", "Hosea", "Hoshea", "הושע", BookGroup.NEVIIM),
        BookInfo("Joe", "Joel", "Yoel", "יואל", BookGroup.NEVIIM),
        BookInfo("Amo", "Amos", "Amos", "עמוס", BookGroup.NEVIIM),
        BookInfo("Oba", "Obadiah", "Ovadyah", "עובדיה", BookGroup.NEVIIM),
        BookInfo("Jon", "Jonah", "Yonah", "יונה", BookGroup.NEVIIM),
        BookInfo("Mic", "Micah", "Michah", "מיכה", BookGroup.NEVIIM),
        BookInfo("Nah", "Nahum", "Nachum", "נחום", BookGroup.NEVIIM),
        BookInfo("Hab", "Habakkuk", "Chavakuk", "חבקוק", BookGroup.NEVIIM),
        BookInfo("Zep", "Zephaniah", "Tzefanyah", "צפניה", BookGroup.NEVIIM),
        BookInfo("Hag", "Haggai", "Chaggai", "חגי", BookGroup.NEVIIM),
        BookInfo("Zec", "Zechariah", "Zecharyah", "זכריה", BookGroup.NEVIIM),
        BookInfo("Mal", "Malachi", "Malachi", "מלאכי", BookGroup.NEVIIM),
        // Ketuvim
        BookInfo("Psa", "Psalms", "Tehillim", "תהילים", BookGroup.KETUVIM),
        BookInfo("Pro", "Proverbs", "Mishlei", "משלי", BookGroup.KETUVIM),
        BookInfo("Job", "Job", "Iyov", "איוב", BookGroup.KETUVIM),
        BookInfo("Son", "Song of Songs", "Shir HaShirim", "שיר השירים", BookGroup.KETUVIM),
        BookInfo("Rth", "Ruth", "Rut", "רות", BookGroup.KETUVIM),
        BookInfo("Lam", "Lamentations", "Eichah", "איכה", BookGroup.KETUVIM),
        BookInfo("Ecc", "Ecclesiastes", "Kohelet", "קהלת", BookGroup.KETUVIM),
        BookInfo("Est", "Esther", "Esther", "אסתר", BookGroup.KETUVIM),
        BookInfo("Dan", "Daniel", "Daniel", "דניאל", BookGroup.KETUVIM),
        BookInfo("Ezr", "Ezra", "Ezra", "עזרא", BookGroup.KETUVIM),
        BookInfo("Neh", "Nehemiah", "Nechemyah", "נחמיה", BookGroup.KETUVIM),
        BookInfo("1Ch", "1 Chronicles", "Divrei HaYamim Alef", "דברי הימים א", BookGroup.KETUVIM),
        BookInfo("2Ch", "2 Chronicles", "Divrei HaYamim Bet", "דברי הימים ב", BookGroup.KETUVIM),
    )

    val byGroup: Map<BookGroup, List<BookInfo>> = all.groupBy { it.group }

    private val byCode = all.associateBy { it.code }
    private val orderByCode = all.withIndex().associate { it.value.code to it.index }

    // Extra spellings people may type when jumping to a reference.
    private val extraAliases = mapOf(
        "Psa" to listOf("psalm"),
        "Son" to listOf("songofsolomon", "songs", "canticles", "sos"),
        "Ecc" to listOf("qoheleth"),
        "Rth" to listOf("rut"),
        "Joe" to listOf("joe"),
    )

    private val aliasesByCode: Map<String, List<String>> = all.associate { b ->
        b.code to (
            listOf(normalize(b.name), normalize(b.translit), b.code.lowercase()) +
                (extraAliases[b.code] ?: emptyList())
            ).distinct()
    }

    fun get(code: String): BookInfo = byCode.getValue(code)
    fun getOrNull(code: String): BookInfo? = byCode[code]
    fun order(code: String): Int = orderByCode[code] ?: Int.MAX_VALUE

    private fun normalize(s: String) = s.lowercase().filter { it.isLetterOrDigit() }

    /** "gen", "Genesis", "1 sam", "Shir HaShirim" ... -> book (or null when unknown/ambiguous). */
    fun find(name: String): BookInfo? {
        val key = normalize(name)
        if (key.isEmpty()) return null
        all.firstOrNull { b -> aliasesByCode.getValue(b.code).any { it == key } }?.let { return it }
        if (key.length < 2) return null
        val matches = all.filter { b -> aliasesByCode.getValue(b.code).any { it.startsWith(key) } }
        return when {
            matches.size == 1 -> matches[0]
            matches.size > 1 && key.length >= 3 -> matches[0]
            else -> null
        }
    }

    private val refRegex =
        Regex("""^\s*([1-3]?\s*[A-Za-z][A-Za-z.\s]*?)\s*(\d+)?(?:\s*[:.]\s*(\d+))?\s*$""")

    /** Parses "Gen 1:1", "psalm 23", "1 sam 3", "Isaiah". Returns null if it isn't a valid reference. */
    fun parseReference(input: String, chapterCounts: Map<String, Int>): Reference? {
        val m = refRegex.matchEntire(input) ?: return null
        val book = find(m.groupValues[1]) ?: return null
        val chapter = m.groupValues[2].toIntOrNull() ?: 1
        val max = chapterCounts[book.code] ?: return null
        if (chapter < 1 || chapter > max) return null
        val verse = m.groupValues[3].toIntOrNull()
        return Reference(book.code, chapter, verse)
    }
}
