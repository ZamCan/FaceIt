package com.zamcan.faceit.text

import androidx.compose.ui.text.font.FontFamily

/**
 * Writing styles for messages, captions and headings.
 *
 * The product owner asked for the thing the "magic sms" apps do — the same words
 * wearing a different voice. That is done here with real typography rather than
 * Unicode look-alike characters: those break search, break screen readers, and a
 * message built from them arrives at the other person as unreadable noise. Every
 * style below is a genuine Android font family plus size, weight, tracking and
 * alignment, so the words stay words.
 *
 * The three families Android guarantees are used. Nothing here bundles a font
 * file, so no font is shipped without a licence and nothing can fail to load.
 */

/** Which surface a writing style is meant for. */
enum class WritingSurface(val label: String) {
    MESSAGE("Messages"),
    CAPTION("Captions"),
    HEADING("Headings"),
    SIGNATURE("Signatures")
}

enum class WritingTone(val label: String) {
    CLASSIC("Classic"),
    ELEGANT("Elegant"),
    CUTE("Cute"),
    BOLD("Bold"),
    QUIET("Quiet"),
    RETRO("Retro")
}

data class WritingStyle(
    val id: String,
    val name: String,
    val tone: WritingTone,
    val surface: WritingSurface,
    val family: FontFamily,
    val letterSpacingEm: Float,
    val lineHeightMultiple: Float,
    /** Relative size against the surrounding text; 1.0 is unchanged. */
    val scale: Float,
    val italic: Boolean,
    val uppercase: Boolean,
    /** Shown to the user as a sample, and used in the gallery as the preview text. */
    val sample: String
) {
    /** Applies the style to a message. Casing is a display decision, not a text edit. */
    fun render(text: String): String = if (uppercase) text.uppercase() else text
}

object WritingStyles {
    private val serif = FontFamily.Serif
    private val sans = FontFamily.SansSerif
    private val mono = FontFamily.Monospace

    val all: List<WritingStyle> = listOf(
        WritingStyle(
            "plain", "Plain", WritingTone.QUIET, WritingSurface.MESSAGE, sans,
            0f, 1.35f, 1f, false, false, "Just the words, exactly as you typed them."
        ),
        WritingStyle(
            "romantic_serif", "Romantic", WritingTone.ELEGANT, WritingSurface.MESSAGE, serif,
            0.01f, 1.55f, 1.06f, true, false, "Some evenings are only worth the walk home."
        ),
        WritingStyle(
            "letterpress", "Letterpress", WritingTone.CLASSIC, WritingSurface.MESSAGE, serif,
            0.02f, 1.45f, 1.02f, false, false, "Thank you for the long letter; I read it twice."
        ),
        WritingStyle(
            "whisper", "Whisper", WritingTone.QUIET, WritingSurface.MESSAGE, sans,
            0.06f, 1.7f, 0.94f, false, false, "no rush — tell me when you're home."
        ),
        WritingStyle(
            "cute_round", "Cute round", WritingTone.CUTE, WritingSurface.MESSAGE, sans,
            0.03f, 1.5f, 1.04f, false, false, "the cat has claimed the warm chair again"
        ),
        WritingStyle(
            "shout", "Shout", WritingTone.BOLD, WritingSurface.MESSAGE, sans,
            0.02f, 1.3f, 1.08f, false, true, "Meeting moved to nine, sorry!"
        ),
        WritingStyle(
            "telegram", "Telegram", WritingTone.RETRO, WritingSurface.MESSAGE, mono,
            0.04f, 1.4f, 0.98f, false, true, "ARRIVING FOUR THIRTY STOP"
        ),
        WritingStyle(
            "receipt", "Receipt", WritingTone.RETRO, WritingSurface.MESSAGE, mono,
            0f, 1.35f, 0.96f, false, false, "2 x coffee  …  7.50\n1 x bread  …  3.00"
        ),
        WritingStyle(
            "poem", "Poem", WritingTone.ELEGANT, WritingSurface.MESSAGE, serif,
            0.04f, 1.75f, 1.02f, true, false, "I kept the shell you found / and the afternoon."
        ),
        WritingStyle(
            "caption_soft", "Soft caption", WritingTone.QUIET, WritingSurface.CAPTION, sans,
            0.03f, 1.3f, 0.92f, false, false, "late light, kitchen window"
        ),
        WritingStyle(
            "caption_serif", "Editorial caption", WritingTone.ELEGANT, WritingSurface.CAPTION, serif,
            0.05f, 1.35f, 0.92f, true, false, "photograph, 35 mm"
        ),
        WritingStyle(
            "heading_strong", "Strong heading", WritingTone.BOLD, WritingSurface.HEADING, sans,
            -0.01f, 1.15f, 1.3f, false, false, "Today"
        ),
        WritingStyle(
            "heading_serif", "Serif heading", WritingTone.CLASSIC, WritingSurface.HEADING, serif,
            0f, 1.2f, 1.32f, false, false, "Today"
        ),
        WritingStyle(
            "heading_wide", "Wide heading", WritingTone.ELEGANT, WritingSurface.HEADING, sans,
            0.14f, 1.2f, 1.05f, false, true, "Today"
        ),
        WritingStyle(
            "signature_script", "Signature", WritingTone.ELEGANT, WritingSurface.SIGNATURE, serif,
            0.02f, 1.2f, 1f, true, false, "— Z."
        ),
        WritingStyle(
            "signature_plain", "Sign-off", WritingTone.QUIET, WritingSurface.SIGNATURE, sans,
            0.02f, 1.2f, 1f, false, false, "Sent from my own phone, slowly."
        )
    )

    val tones: List<WritingTone> get() = all.map { it.tone }.distinct()

    fun find(id: String?): WritingStyle =
        all.firstOrNull { it.id == id } ?: all.first()

    fun forSurface(surface: WritingSurface): List<WritingStyle> =
        all.filter { it.surface == surface }

    fun byTone(tone: WritingTone?): List<WritingStyle> =
        if (tone == null) all else all.filter { it.tone == tone }
}
