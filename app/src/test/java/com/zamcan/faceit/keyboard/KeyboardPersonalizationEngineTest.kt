package com.zamcan.faceit.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPersonalizationEngineTest {
    @Test fun validatesProfileGeometryBounds() {
        val profile = KeyboardPersonalizationEngine.validate(
            KeyboardProfile(keyHeightDp = 100, keyGapDp = 0, cornerRadiusDp = 40)
        )
        assertEquals(72, profile.keyHeightDp)
        assertEquals(2, profile.keyGapDp)
        assertEquals(28, profile.cornerRadiusDp)
    }

    @Test fun providesDistinctSupportedLayouts() {
        assertEquals("qwertyuiop", KeyboardPersonalizationEngine.mapCharacters(KeyboardLayout.QWERTY).first())
        assertEquals("qwertzuiop", KeyboardPersonalizationEngine.mapCharacters(KeyboardLayout.QWERTZ).first())
        assertEquals("azertyuiop", KeyboardPersonalizationEngine.mapCharacters(KeyboardLayout.AZERTY).first())
    }

    @Test fun smsTransportBodyRemainsPlainTextAndUnchanged() {
        val original = "Meet at 10:30 — إن شاء الله"
        assertEquals(original, SmsDraft("+255700000000", original).transportBody())
        assertTrue(KeyboardProfile().hapticsEnabled)
    }
}
