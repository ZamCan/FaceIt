package com.zamcan.faceit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalEnvironmentTest {
    @Test fun savesAndRestoresIndependentVisualLayers() {
        val original = PersonalEnvironment(
            name = "The Stone Guardian",
            background = "Football",
            mainObject = "Lion sculpture",
            material = "Natural stone",
            motion = "Subtle",
            widgetsEnabled = true,
            iconsEnabled = true,
            dialerStyle = "Ceramic Sculpt",
            resourceProfile = "Balanced",
            selectedArtworkFile = "wallpaper-01.png",
            selectedObjectFile = "stone-lion.png"
        )
        assertEquals(original, PersonalEnvironment.fromJson(original.toJson()))
    }

    @Test fun oldSavedEnvironmentRemainsCompatible() {
        val old = """{"name":"My World","background":"Nature","mainObject":"None"}"""
        val restored = PersonalEnvironment.fromJson(old)
        assertEquals("My World", restored.name)
        assertEquals("Nature", restored.background)
        assertEquals("None", restored.mainObject)
        assertEquals("Balanced", restored.resourceProfile)
        assertNull(restored.selectedArtworkFile)
    }

    @Test fun invalidSavedDataFallsBackSafely() {
        assertEquals(PersonalEnvironment(), PersonalEnvironment.fromJson("{not-json"))
    }
    @Test fun sampleCompositionsPreserveTheirDistinctVisualDirections() {
        val stone = CompositionPresets.find("The Stone Guardian")!!
        val madinah = CompositionPresets.find("Madinah Serenity")!!
        val geometry = CompositionPresets.find("Heritage Geometry")!!

        assertEquals("Football", stone.background)
        assertEquals("Lion sculpture", stone.mainObject)
        assertEquals("Natural stone", stone.material)
        assertEquals("Heritage architecture", madinah.background)
        assertEquals("Marble", madinah.material)
        assertEquals("Geometry", geometry.background)
    }


    @Test
    fun defaultCompositionIsStoneGuardian() {
        // Rescued from a duplicate test class that shadowed this one: two classes with
        // the same simple name in one module is a build smell, so the assertion lives
        // here now and the duplicate is gone.
        val v = PersonalEnvironment()
        assertEquals("Football", v.background)
        assertEquals("Lion sculpture", v.mainObject)
        assertEquals("Natural stone", v.material)
        assertEquals("Subtle", v.motion)
        assertTrue(v.widgetsEnabled)
        assertTrue(v.iconsEnabled)
    }
}
