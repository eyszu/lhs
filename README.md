# Light Hebrew Sefer (LHS)

Offline Android Bible reader (Kotlin + Jetpack Compose) for the bundled NLT Old Testament database,
organised as the Tanakh: Torah, Nevi'im, Ketuvim.

## Build
1. Open this folder in Android Studio (Ladybug or newer). Let Gradle sync (it will offer to create the wrapper).
2. Run on a device/emulator (Android 7.0+), or Build > Build APK(s).
   For a release build use Build > Generate Signed Bundle / APK.

## Features
- Themes: Light, Dark, Dusk, Cool
- Text: font family, size (14-34 sp), line spacing - from Settings or the "Aa" button in the reader
- Tap verses to select (several allowed) -> Copy / Bookmark / Share
- Bookmarks tab (copy, open, remove with undo)
- Search: all words must match, "quoted phrases", filter by Torah / Nevi'im / Ketuvim,
  or type a reference ("Isa 53", "1 Sam 17:45") to jump there
- Divine name display: the DB stores it as Paleo-Hebrew letters; choose יהוה / YHWH / HaShem / LORD / original

## Data
`app/src/main/assets/nlt.db` is the supplied database plus an index on (book, chapter, verse),
WAL mode removed, and one garbled word fixed (Deut 20:7 "fiancée").
The supplied database contains the 39 Old Testament books only.

## Install pre build
- download codes (zip)
- unzip and copy LHS.apk to your phone
- install LHS.apk