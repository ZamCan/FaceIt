package com.zamcan.faceit.widget

/**
 * Clock widget finishes.
 *
 * These are the only appearance entries in the whole product that are literally
 * applied to something outside an in-app preview: the RemoteViews provider reads
 * [id] and paints the real home-screen widget with [background], [time] and
 * [date]. Everything else in the style studio is honest about being a preview;
 * this one is not a preview, so it is kept deliberately small, contrast-checked
 * and stable — an id here is persisted inside every placed widget instance.
 *
 * Nine finishes, three material families:
 *  - Glazed   dark, glass-forward
 *  - Ceramic  light and warm, high legibility
 *  - Satin    soft mid-tones for the colourful themes
 *
 * Adding a finish is safe; renaming an id is not, because an existing widget on
 * the user's home screen stores the id it was created with. Ids are therefore
 * frozen strings and [find] falls back to the first entry rather than throwing.
 */

data class FaceItClockStyle(
    val id: String,
    val label: String,
    val family: String,
    val background: Int,
    val time: Int,
    val date: Int
) {
    /** Time-on-background contrast, kept at or above AAA for a glanceable widget. */
    val timeContrast: Double get() = com.zamcan.faceit.themes.ThemeContrast.ratio(time, background)

    /** Date line contrast; a supporting line may be quieter than the time itself. */
    val dateContrast: Double get() = com.zamcan.faceit.themes.ThemeContrast.ratio(date, background)
}

object FaceItClockWidgetStyles {

    const val DEFAULT_ID = "Obsidian"

    val all: List<FaceItClockStyle> = listOf(
        // ---- Glazed: deep surfaces, luminous numerals
        FaceItClockStyle(
            "Obsidian", "Obsidian Glass", "Glazed",
            0xE622252D.toInt(), 0xFFF4F0FA.toInt(), 0xFFA78BFA.toInt()
        ),
        FaceItClockStyle(
            "Midnight", "Midnight Silk", "Glazed",
            0xE6101524.toInt(), 0xFFF0F4FF.toInt(), 0xFF7FD4C8.toInt()
        ),
        FaceItClockStyle(
            "Noir", "Noir", "Glazed",
            0xF0121214.toInt(), 0xFFFFFFFF.toInt(), 0xFF9E9AA8.toInt()
        ),
        FaceItClockStyle(
            "Mint", "Pearl Mint", "Glazed",
            0xE6233737.toInt(), 0xFFF2FAF6.toInt(), 0xFF76DCCB.toInt()
        ),
        // ---- Ceramic: light, warm, made for daylight
        FaceItClockStyle(
            "Ceramic", "Warm Ceramic", "Ceramic",
            0xEAE7E0D3.toInt(), 0xFF292A2D.toInt(), 0xFF78664D.toInt()
        ),
        FaceItClockStyle(
            "Paper", "Warm Paper", "Ceramic",
            0xECF5F0E7.toInt(), 0xFF221F1A.toInt(), 0xFF8A6A46.toInt()
        ),
        FaceItClockStyle(
            "Pearl", "Pearl Shell", "Ceramic",
            0xECF6ECEE.toInt(), 0xFF2A2027.toInt(), 0xFF9A6079.toInt()
        ),
        // ---- Satin: soft mid-tones that survive a busy wallpaper
        FaceItClockStyle(
            "Satin", "Rose Satin", "Satin",
            0xE83A2731.toInt(), 0xFFFFF1F5.toInt(), 0xFFF0A8C0.toInt()
        ),
        FaceItClockStyle(
            "Sage", "Sage Satin", "Satin",
            0xE823322C.toInt(), 0xFFF0F8F2.toInt(), 0xFF9BD6B5.toInt()
        )
    )

    val families: List<String> get() = all.map { it.family }.distinct()

    fun find(id: String?): FaceItClockStyle =
        all.firstOrNull { it.id == id } ?: all.first()
}
