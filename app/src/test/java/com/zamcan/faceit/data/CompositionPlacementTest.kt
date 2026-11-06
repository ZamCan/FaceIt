package com.zamcan.faceit.data

import org.junit.Assert.*
import org.junit.Test

class CompositionPlacementTest {
    @Test fun placementSurvivesSaveAndDoesNotChangeIndependentSources() {
        val value = PersonalEnvironment(selectedArtworkFile = "background.png", selectedObjectFile = "object.webp",
            objectScale = 1.4f, objectX = .2f, objectY = .7f, objectOpacity = .6f, cropX = .8f, cropY = .1f)
        assertEquals(value, PersonalEnvironment.fromJson(value.toJson()))
        val changed = value.copy(objectX = .9f)
        assertEquals(value.selectedArtworkFile, changed.selectedArtworkFile)
        assertEquals(value.selectedObjectFile, changed.selectedObjectFile)
        assertEquals(value.material, changed.material)
        assertEquals(value.cropX, changed.cropX, .001f)
    }
    @Test fun malformedPlacementIsBoundedOnReload() {
        val value = PersonalEnvironment.fromJson("""{"objectScale":999,"objectX":-8,"objectY":2,"objectOpacity":-1,"cropX":4,"cropY":-5}""")
        assertEquals(2f, value.objectScale, .001f)
        assertEquals(0f, value.objectX, .001f)
        assertEquals(1f, value.objectY, .001f)
        assertEquals(0f, value.objectOpacity, .001f)
        assertEquals(1f, value.cropX, .001f)
        assertEquals(0f, value.cropY, .001f)
    }
    @Test fun olderScenesKeepOriginalPlacement() {
        val value = PersonalEnvironment.fromJson("""{"name":"Legacy"}""")
        assertEquals(1f, value.objectScale, .001f)
        assertEquals(.5f, value.objectX, .001f)
        assertEquals(.38f, value.objectY, .001f)
        assertEquals(1f, value.objectOpacity, .001f)
    }
}

