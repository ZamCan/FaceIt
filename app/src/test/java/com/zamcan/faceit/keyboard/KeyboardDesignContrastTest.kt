package com.zamcan.faceit.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrast maths for key legends.
 *
 * Accessibility is a release requirement in AGENTS.md §17, and "the colours look
 * fine" is not a check. These tests pin the WCAG 2.1 formula so a future palette
 * cannot quietly drop below AA.
 */
class KeyboardDesignContrastTest {
    @Test fun blackOnWhiteIsTwentyOne() {
        assertEquals(21.0, KeyboardDesignEngine.contrastRatio(0x000000L, 0xFFFFFFL), 0.01)
    }

    @Test fun identicalColoursHaveNoContrast() {
        assertEquals(1.0, KeyboardDesignEngine.contrastRatio(0x76DCCBL, 0x76DCCBL), 0.001)
    }

    @Test fun contrastIsSymmetric() {
        val a = KeyboardDesignEngine.contrastRatio(0x17191FL, 0xF3F1F7L)
        val b = KeyboardDesignEngine.contrastRatio(0xF3F1F7L, 0x17191FL)
        assertEquals(a, b, 0.0001)
    }

    @Test fun everyShippedPaletteKeepsLegendsReadable() {
        KeyboardPalettes.all.forEach { (id, palette) ->
            val ratio = KeyboardDesignEngine.contrastRatio(palette.keyText, palette.keySurface)
            assertTrue("$id legend contrast is $ratio, below AA 4.5", ratio >= 4.5)
            assertTrue("$id legend contrast is not flagged accessible", KeyboardDesignEngine.legendIsAccessible(palette))
        }
    }

    @Test fun everyShippedPaletteKeepsAccentsVisible() {
        KeyboardPalettes.all.forEach { (id, palette) ->
            val ratio = KeyboardDesignEngine.contrastRatio(palette.accent, palette.background)
            assertTrue("$id accent contrast is $ratio, below 3.0", ratio >= 3.0)
        }
    }
}
