package com.zamcan.faceit.data

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class SceneLightingTest {
    @Test fun enabledLightingSurvivesSceneAndLibraryRoundTrips() {
        val lighting = SceneLighting(true, LightDirection.UPPER_RIGHT, .8f, .3f, -.6f)
        val value = PersonalEnvironment(selectedArtworkFile = "mine.png", selectedObjectFile = "object.webp", lighting = lighting)
        assertEquals(value, PersonalEnvironment.fromJson(value.toJson()))
        val snapshot = SavedComposition("scene", value, 123)
        assertEquals(listOf(snapshot), SavedCompositionCodec.decode(SavedCompositionCodec.encode(listOf(snapshot))))
    }

    @Test fun oldScenesKeepTheirOriginalAppearance() {
        assertFalse(PersonalEnvironment.fromJson("""{"name":"Legacy"}""").lighting.enabled)
        assertEquals(SceneLighting(), SceneLighting.fromJson(null))
    }

    @Test fun invalidSavedControlsAreBoundedAndUnknownDirectionsFallBack() {
        val value = SceneLighting.fromJson(JSONObject("""{"enabled":true,"direction":"unknown","intensity":9,"softness":-3,"warmth":-9}"""))
        assertTrue(value.enabled)
        assertEquals(LightDirection.UPPER_LEFT, value.direction)
        assertEquals(1f, value.intensity, .001f)
        assertEquals(0f, value.softness, .001f)
        assertEquals(-1f, value.warmth, .001f)
    }

    @Test fun changingLightingKeepsEveryIndependentSourceAndPlacement() {
        val original = PersonalEnvironment(selectedArtworkFile = "background.png", selectedObjectFile = "object.png",
            material = "Marble", objectX = .2f, cropY = .9f, nameText = "Amina")
        val changed = original.copy(lighting = SceneLighting(enabled = true))
        assertEquals(original, changed.copy(lighting = original.lighting))
        assertEquals(original, EditHistory(original).record(changed).undo().current)
    }
}
