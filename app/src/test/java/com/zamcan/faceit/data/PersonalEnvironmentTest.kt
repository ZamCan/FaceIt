package com.zamcan.faceit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            selectedArtworkFile = "wallpaper-01.png"
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
}
