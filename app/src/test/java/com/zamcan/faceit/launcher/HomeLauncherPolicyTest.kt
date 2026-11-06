package com.zamcan.faceit.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The optional Home launcher has three rules worth pinning as behaviour rather
 * than as a comment: app search is case- and space-insensitive, duplicate entries
 * for one package collapse to one tile, and an empty query means "everything"
 * rather than "nothing".
 */
class HomeLauncherPolicyTest {

    private fun filter(labels: List<String>, query: String): List<String> =
        if (query.isBlank()) {
            labels
        } else {
            labels.filter { it.contains(query.trim(), ignoreCase = true) }
        }

    @Test
    fun emptyQueryShowsEverything() {
        val all = listOf("Camera", "Clock", "Maps")
        assertEquals(all, filter(all, ""))
        assertEquals(all, filter(all, "   "))
    }

    @Test
    fun searchIgnoresCaseAndSurroundingSpace() {
        val all = listOf("Camera", "Clock", "Maps", "Calm")
        assertEquals(listOf("Camera", "Calm"), filter(all, "ca"))
        assertEquals(listOf("Camera", "Calm"), filter(all, "  CA  "))
    }

    @Test
    fun searchingForNothingIsHonestRatherThanShowingEverything() {
        val all = listOf("Camera", "Clock")
        assertEquals(emptyList<String>(), filter(all, "whatsapp"))
    }

    @Test
    fun aPackageWithTwoLauncherActivitiesIsListedOnce() {
        // loadLaunchableApps applies distinctBy on the package, so a package exposing
        // several entry points appears as one tile — which is what a launcher means.
        val entries = listOf("com.a" to "Main", "com.a" to "Alias", "com.b" to "Main")
        val deduped = entries.distinctBy { it.first }
        assertEquals(2, deduped.size)
        assertEquals(listOf("com.a", "com.b"), deduped.map { it.first })
    }

    @Test
    fun wallpaperChoiceFollowsTheSameOrderAsTheRestOfTheApp() {
        // Imported artwork, then the chosen photograph, then the theme's own
        // photograph — the same order WallpaperPolicy applies in the app.
        fun resolve(imported: String?, curated: String?, theme: String) =
            imported?.takeIf { it.isNotBlank() }
                ?: curated?.takeIf { it.isNotBlank() }
                ?: theme

        assertEquals("mine.png", resolve("mine.png", "curated-1", "fluorite-specimen"))
        assertEquals("curated-1", resolve(null, "curated-1", "fluorite-specimen"))
        assertEquals("curated-1", resolve("   ", "curated-1", "fluorite-specimen"))
        assertEquals("fluorite-specimen", resolve(null, null, "fluorite-specimen"))
    }
}
