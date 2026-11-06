package com.zamcan.faceit.graphics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The name engine's contract, tested without an Android runtime.
 *
 * Everything that can go wrong with a name is a text problem before it is a
 * drawing problem: an empty field, a name longer than the screen, a name in a
 * script the app has never seen, a name that differs only in spacing. Those are
 * all checked here, because the renderer is only as correct as the string it is
 * handed.
 */
class NameMonogramTest {

    @Test
    fun everyFinishIsReachableAndUnique() {
        val ids = NameFinish.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(15, NameFinish.entries.size)
        NameFinish.entries.forEach { finish ->
            assertEquals(finish, NameFinish.find(finish.id))
        }
    }

    @Test
    fun anUnknownFinishFallsBackRatherThanCrashing() {
        // A saved environment can outlive a finish being renamed; it must still draw.
        assertEquals(NameFinish.ROSE_GOLD, NameFinish.find("unobtainium"))
        assertEquals(NameFinish.ROSE_GOLD, NameFinish.find(null))
    }

    @Test
    fun theTypedNameIsNeverRewritten() {
        val design = NameDesign(text = "  Naomi  ")
        // Whitespace is normalised; letters are not touched. Case is a display
        // decision, not an edit, and the engine never substitutes characters.
        assertEquals("NAOMI", design.displayText())
        val mixed = NameDesign(text = "Ng'ang'a", uppercase = false)
        assertEquals("Ng'ang'a", mixed.displayText())
    }

    @Test
    fun aNameInAnotherScriptSurvives() {
        // Font fallback handles this at draw time; the design must not mangle it.
        listOf("佐藤", "Ольга", "Šimon", "عائشة", "አማርኛ").forEach { name ->
            assertEquals(name, NameDesign(text = name, uppercase = false).displayText())
        }
    }

    @Test
    fun anEmptyNameStillProducesSomethingToDraw() {
        assertEquals("YOUR NAME", NameDesign(text = "   ").displayText())
        assertEquals("YOUR NAME", NameDesign(text = "").displayText())
        assertEquals("Your name", NameDesign(text = "", uppercase = false).displayText())
    }

    @Test
    fun longNamesAreMeasuredAsLongRatherThanClipped() {
        val short = NameDesign(text = "Ada", uppercase = false)
        val long = NameDesign(text = "Wema Mwakyusa Mushi", uppercase = false)
        assertTrue(long.effectiveLength() > short.effectiveLength())
        // The engine fits by width, so the only thing it needs is the length.
        assertEquals(19, long.effectiveLength())
    }

    @Test
    fun scriptsAndSettingsRoundTripByIdAndNeverReturnNull() {
        NameScript.entries.forEach { assertEquals(it, NameScript.find(it.id)) }
        NameSetting.entries.forEach { assertEquals(it, NameSetting.find(it.id)) }
        assertNotNull(NameScript.find("unknown"))
        assertNotNull(NameSetting.find("unknown"))
    }

    @Test
    fun wideTrackingIsWiderThanTightForEveryScript() {
        NameScript.entries.forEach { script ->
            assertTrue(
                "${script.label} should track wider when asked",
                NameSetting.WIDE.tracking(script) > NameSetting.TIGHT.tracking(script)
            )
        }
    }

    @Test
    fun theBackdropIsDarkEnoughForMetalToReadOn() {
        NameFinish.entries.forEach { finish ->
            val backdrop = NameMonogram.backdrop(finish)
            val red = (backdrop shr 16) and 0xFF
            val green = (backdrop shr 8) and 0xFF
            val blue = backdrop and 0xFF
            val luminance = (red * 299 + green * 587 + blue * 114) / 1000
            assertTrue(
                "${finish.label} backdrop is too bright for a highlight to register",
                luminance < 40
            )
        }
    }

    @Test
    fun thePreviewBandReservesRoomWithoutEatingTheScreen() {
        val fraction = NameMonogram.reservedBandFraction()
        assertTrue(fraction > 0.1f)
        assertTrue(fraction < 0.3f)
    }

    @Test
    fun shimmerStaysInsideEightBitsForEveryFinish() {
        NameFinish.entries.forEach { finish ->
            (0..20).forEach { step ->
                val alpha = NameMonogram.shimmerAlpha(finish, step / 20f)
                assertTrue("alpha out of range for ${finish.label}", alpha in 0..255)
            }
        }
    }

    @Test
    fun theEnvironmentTurnsTheNameOffWithoutLosingIt() {
        val off = com.zamcan.faceit.data.PersonalEnvironment(nameEnabled = false, nameText = "Naomi")
        assertNull(off.nameDesignOrNull())
        // Turning it back on restores the words that were typed, not a default.
        val on = off.copy(nameEnabled = true)
        assertEquals("NAOMI", on.nameDesignOrNull()?.displayText())
    }

    @Test
    fun aNameDesignSurvivesASaveAndReload() {
        val original = com.zamcan.faceit.data.PersonalEnvironment(
            nameEnabled = true,
            nameText = "Amina",
            nameFinishId = "sapphire",
            nameScriptId = "serif",
            nameSettingId = "wide",
            nameAnchor = 0.71f,
            namePlaque = false,
            nameGroundId = "generated-sapphire-halo-ring"
        )
        val reloaded = com.zamcan.faceit.data.PersonalEnvironment.fromJson(original.toJson())
        val design = reloaded.nameDesignOrNull()
        assertNotNull(design)
        assertEquals("AMINA", design!!.displayText())
        assertEquals(NameFinish.SAPPHIRE, design.finish)
        assertEquals(NameScript.SERIF, design.script)
        assertEquals(NameSetting.WIDE, design.setting)
        assertEquals(0.71f, design.anchor, 0.001f)
        assertFalse(design.plaque)
        assertEquals("generated-sapphire-halo-ring", design.groundId)
    }

    @Test
    fun anEnvironmentSavedBeforeTheNameEngineExistedStillLoads() {
        val legacy = """{"name":"The Stone Guardian","themeId":"pearl_mint"}"""
        val loaded = com.zamcan.faceit.data.PersonalEnvironment.fromJson(legacy)
        assertFalse(loaded.nameEnabled)
        assertNull(loaded.nameDesignOrNull())
    }

    @Test
    fun theAnchorCannotBeSavedOffTheScreen() {
        val far = com.zamcan.faceit.data.PersonalEnvironment.fromJson(
            """{"nameEnabled":true,"nameText":"Ada","nameAnchor":4.2}"""
        )
        val anchor = far.nameDesignOrNull()!!.anchor
        assertTrue("anchor must stay on screen, was $anchor", anchor in 0.08f..0.92f)
    }
}
