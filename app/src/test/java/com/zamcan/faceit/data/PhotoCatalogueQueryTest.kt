package com.zamcan.faceit.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoCatalogueQueryTest {
    private fun photo(id: String, title: String, category: String, synthetic: Boolean = false) =
        PhotoWallpaper(id, category, title, title, "Amina", "", "cc0", "1.0",
            "https://creativecommons.org/publicdomain/zero/1.0/", "", false,
            "photos/$id.jpg", "photos/thumbs/$id.jpg", 1080, 1920, synthetic)

    private val photos = listOf(
        photo("a", "Rose quartz", "minerals"),
        photo("b", "Rose gold", "jewellery", synthetic = true),
        photo("c", "Factory hall", "industrial")
    )

    @Test fun searchMatchesAllTermsAcrossMetadataWithoutChangingOrder() {
        assertEquals(listOf("a", "b"), PhotoCatalogueQuery.filter(photos, "  ROSE  Amina ").map { it.id })
    }
    @Test fun categoryAndFavouriteFiltersCombineWithSearch() {
        assertEquals(listOf("b"), PhotoCatalogueQuery.filter(photos, "rose", "jewellery",
            favouritesOnly = true, favourites = setOf("a", "b")).map { it.id })
    }
    @Test fun generatedImagesCanBeFoundWithoutBeingCalledPhotographs() {
        assertEquals(listOf("b"), PhotoCatalogueQuery.filter(photos, "generated").map { it.id })
        assertEquals(listOf("a", "c"), PhotoCatalogueQuery.filter(photos, "photograph").map { it.id })
    }
    @Test fun staleFavouriteIdsDoNotCreatePlaceholderEntries() {
        assertEquals(emptyList<PhotoWallpaper>(), PhotoCatalogueQuery.filter(photos,
            favouritesOnly = true, favourites = setOf("removed")))
    }
}

