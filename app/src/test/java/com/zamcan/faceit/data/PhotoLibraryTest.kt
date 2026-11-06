package com.zamcan.faceit.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The photographic pack is other people's work shipped under a licence. These
 * tests guard the two things that would make shipping it wrong: a photograph
 * with incomplete attribution, and a licence we are not allowed to bundle.
 */
class PhotoLibraryTest {

    private fun entry(
        id: String = "amethyst-cluster-brazil",
        licence: String = "cc-by",
        creator: String = "Jane Doe",
        licenceUrl: String = "https://creativecommons.org/licenses/by/4.0/",
        shareAlike: Boolean = false
    ) = JSONObject().apply {
        put("id", id)
        put("category", "minerals")
        put("subject", "amethyst specimen")
        put("title", "Amethyst cluster, Brazil")
        put("creator", creator)
        put("creatorUrl", "https://commons.wikimedia.org/wiki/User:JaneDoe")
        put("licence", licence)
        put("licenceVersion", "4.0")
        put("licenceUrl", licenceUrl)
        put("source", "https://commons.wikimedia.org/wiki/File:Amethyst.jpg")
        put("shareAlike", shareAlike)
        put("asset", "photos/$id.jpg")
        put("thumbnail", "photos/thumbs/$id.jpg")
        put("width", 2560)
        put("height", 1707)
    }

    private fun index(vararg entries: JSONObject): String =
        JSONObject().apply {
            put("count", entries.size)
            put("photos", org.json.JSONArray(entries.toList()))
        }.toString()

    @Test fun parsesTheIndexItWasGiven() {
        val photos = PhotoLibrary.parse(index(entry()))
        assertEquals(1, photos.size)
        val photo = photos.single()
        assertEquals("minerals", photo.category)
        assertEquals("Jane Doe", photo.creator)
        assertEquals("cc-by", photo.licence)
        assertEquals(2560, photo.width)
        assertFalse(photo.shareAlike)
    }

    @Test fun creditLineNamesTheMakerAndTheLicence() {
        val photo = PhotoLibrary.parse(index(entry())).single()
        assertTrue(photo.credit.contains("Jane Doe"))
        assertTrue(photo.credit.contains("CC-BY"))
    }

    @Test fun shareAlikeIsCarriedThroughRatherThanAssumedAway() {
        val photo = PhotoLibrary.parse(index(entry(licence = "cc-by-sa", shareAlike = true))).single()
        assertTrue(photo.shareAlike)
        // hasShareAlike takes a Context; the list-level check is what a JVM test
        // can make, and it is the same predicate the Context overload delegates to.
        assertTrue(PhotoLibrary.parse(index(entry(shareAlike = true))).any { it.shareAlike })
    }

    @Test fun aPackOfPermissiveLicencesReportsNoShareAlike() {
        val photos = PhotoLibrary.parse(
            index(entry(id = "a", licence = "cc0"), entry(id = "b", licence = "cc-by"))
        )
        assertEquals(2, photos.size)
        assertFalse(photos.any { it.shareAlike })
    }

    @Test fun emptyOrBrokenIndexYieldsNoPhotographsRatherThanCrashing() {
        assertEquals(0, PhotoLibrary.parse("""{"photos":[]}""").size)
        assertEquals(0, PhotoLibrary.parse("{}").size)
    }

    @Test fun entriesWithoutAFileAreDroppedInsteadOfShowingBrokenTiles() {
        val broken = entry(id = "no-file").apply { remove("asset") }
        assertEquals(0, PhotoLibrary.parse(index(broken)).size)
    }

    @Test fun nonCommercialAndNoDerivativesNeverReachTheLibrary() {
        // These are dropped at parse time, so a gallery built from this index has
        // no tile to show rather than a tile that would be unlawful to ship.
        for (licence in listOf("cc-by-nc", "cc-by-nd", "gfdl", "all-rights-reserved")) {
            assertEquals(
                "$licence must not reach the gallery",
                0,
                PhotoLibrary.parse(index(entry(licence = licence))).size
            )
        }
        assertEquals(1, PhotoLibrary.parse(index(entry(licence = "cc0"))).size)
    }

    @Test fun aGeneratedImageSurvivesWithoutAPhotographer() {
        // Generated work is ours and CC0, so it needs no attribution -- but it has
        // to be marked, or the gallery would present it as someone's photograph.
        val generated = entry(id = "generated-turbine-hall", creator = "").apply {
            put("synthetic", true)
            put("provider", "generated")
        }
        val photos = PhotoLibrary.parse(index(generated))
        assertEquals(1, photos.size)
        assertTrue(photos.single().synthetic)
    }

    @Test fun anUncreditedPhotographIsDroppedHoweverFreeItsLicence() {
        assertEquals(0, PhotoLibrary.parse(index(entry(creator = " "))).size)
    }

    @Test fun theBundledIndexIsShippableFromEndToEnd() {
        // The real asset, not a fixture: every bundled photograph must survive the
        // same gate that the fixture tests exercise.
        val raw = java.io.File(
            "src/main/assets/photos/photo_index.json"
        ).readText()
        val photos = PhotoLibrary.parse(raw)
        assertTrue("the pack should not be empty", photos.size > 100)
        assertTrue("every bundled photo must be shippable", photos.all { it.isShippable })
        assertTrue("every bundled photo must be credited", photos.all { it.creator.isNotBlank() })
    }
}
