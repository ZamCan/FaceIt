package com.zamcan.faceit.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The clock widget is the one thing in the appearance studio that is applied for
 * real, so its catalogue is held to a stricter standard than the previews: ids are
 * frozen because they are persisted inside placed widget instances, contrast is
 * AAA for the time, and a missing id must fall back rather than blank the widget.
 */
class ClockWidgetStylesTest {

    @Test
    fun idsAreUnique() {
        val ids = FaceItClockWidgetStyles.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun theThreeOriginalIdsAreNeverRenamed() {
        // Existing widgets on users' home screens store these exact strings.
        listOf("Obsidian", "Ceramic", "Mint").forEach { id ->
            assertEquals(id, FaceItClockWidgetStyles.find(id).id)
        }
    }

    @Test
    fun theDefaultIdExists() {
        assertEquals(
            FaceItClockWidgetStyles.DEFAULT_ID,
            FaceItClockWidgetStyles.find(FaceItClockWidgetStyles.DEFAULT_ID).id
        )
    }

    @Test
    fun timeIsLegibleAgainstEveryBackground() {
        FaceItClockWidgetStyles.all.forEach { style ->
            assertTrue(
                "${style.label}: time contrast ${style.timeContrast}",
                style.timeContrast >= 7.0
            )
        }
    }

    @Test
    fun theDateLineIsLegibleToo() {
        FaceItClockWidgetStyles.all.forEach { style ->
            assertTrue(
                "${style.label}: date contrast ${style.dateContrast}",
                style.dateContrast >= 3.0
            )
        }
    }

    @Test
    fun thereAreLightAndDarkFinishes() {
        val families = FaceItClockWidgetStyles.families
        assertTrue(families.size >= 3)
        assertTrue(
            "expected a light finish for daylight",
            FaceItClockWidgetStyles.all.any { style ->
                com.zamcan.faceit.themes.ThemeContrast.ratio(style.background, 0xFFFFFFFF) < 1.6
            }
        )
    }

    @Test
    fun unknownIdFallsBackInsteadOfCrashing() {
        assertEquals(FaceItClockWidgetStyles.all.first().id, FaceItClockWidgetStyles.find("no_such_finish").id)
        assertEquals(FaceItClockWidgetStyles.all.first().id, FaceItClockWidgetStyles.find(null).id)
    }
}
