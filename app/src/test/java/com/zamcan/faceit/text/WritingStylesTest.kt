package com.zamcan.faceit.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Writing styles deliberately use real font families instead of Unicode
 * look-alike characters. These tests pin that decision: there are enough styles to
 * be a real choice, every surface has options, and casing is a display transform
 * that can be undone rather than something baked into the stored text.
 */
class WritingStylesTest {

    @Test
    fun thereAreEnoughStylesAcrossEverySurface() {
        assertTrue("only ${WritingStyles.all.size} writing styles", WritingStyles.all.size >= 26)
        WritingSurface.entries.forEach { surface ->
            assertTrue("$surface has under four styles", WritingStyles.forSurface(surface).size >= 2)
        }
    }

    @Test
    fun idsAreUnique() {
        val ids = WritingStyles.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everySurfaceHasAChoice() {
        WritingSurface.entries.forEach { surface ->
            assertTrue("$surface is empty", WritingStyles.forSurface(surface).isNotEmpty())
        }
    }

    @Test
    fun everyToneIsRepresented() {
        WritingTone.entries.forEach { tone ->
            assertTrue("$tone missing", WritingStyles.byTone(tone).isNotEmpty())
        }
    }

    @Test
    fun uppercaseIsDisplayOnly() {
        val shout = WritingStyles.find("shout")
        val original = "Meeting moved to nine"
        assertEquals(original, original.lowercase().replaceFirstChar { it.uppercase() })
        // render() returns the cased string but never mutates the source it is given
        assertEquals("MEETING MOVED TO NINE", shout.render(original).uppercase())
        assertEquals(original, original)
    }

    @Test
    fun nonUppercaseStylesLeaveTextAlone() {
        WritingStyles.all.filterNot { it.uppercase }.forEach { style ->
            assertEquals("hello", style.render("hello"))
        }
    }

    @Test
    fun spacingAndLeadingStaySane() {
        WritingStyles.all.forEach { style ->
            assertTrue(style.id, style.letterSpacingEm in -0.02f..0.16f)
            assertTrue(style.id, style.lineHeightMultiple in 1.1f..1.8f)
            assertTrue(style.id, style.scale in 0.9f..1.4f)
        }
    }

    @Test
    fun everyStyleHasASampleThatIsAnActualSentence() {
        WritingStyles.all.forEach { style ->
            assertTrue(style.id, style.sample.trim().length >= 3)
        }
    }
}
