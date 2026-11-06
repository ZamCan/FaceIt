package com.zamcan.faceit.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CompositionBackgroundPolicyTest {
    @Test fun importedArtworkWinsOverThemeAndSelectedPhoto() {
        val value = PersonalEnvironment(selectedArtworkFile = "mine.png", curatedPhotoId = "photo")
        assertEquals(CompositionBackground.Imported("mine.png"), CompositionBackgroundPolicy.resolve(value, "theme"))
    }
    @Test fun proceduralSceneIsAnExplicitChoiceThatSurvivesReload() {
        val value = PersonalEnvironment(useProceduralBackground = true, curatedPhotoId = "photo")
        val reloaded = PersonalEnvironment.fromJson(value.toJson())
        assertEquals(CompositionBackground.Procedural, CompositionBackgroundPolicy.resolve(reloaded, "theme"))
    }
    @Test fun selectedPhotoWinsOverTheme() {
        assertEquals(CompositionBackground.Bundled("photo"),
            CompositionBackgroundPolicy.resolve(PersonalEnvironment(curatedPhotoId = "photo"), "theme"))
    }
    @Test fun legacyEnvironmentKeepsItsThemePhoto() {
        val value = PersonalEnvironment.fromJson("""{"name":"Legacy"}""")
        assertEquals(CompositionBackground.Bundled("theme"), CompositionBackgroundPolicy.resolve(value, "theme"))
    }
    @Test fun nameGroundIsIndependentAndOnlyAppliesWhileNameIsEnabled() {
        val value = PersonalEnvironment(nameEnabled = true, nameGroundId = "ground", selectedArtworkFile = "mine.png")
        assertEquals(CompositionBackground.Bundled("ground"), CompositionBackgroundPolicy.resolve(value, "theme"))
        assertEquals(CompositionBackground.Imported("mine.png"), CompositionBackgroundPolicy.resolve(value.copy(nameEnabled = false), "theme"))
    }
}

