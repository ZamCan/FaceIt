package com.zamcan.faceit.data

/** Offline catalogue filtering; stable ids keep favourites independent from titles. */
object PhotoCatalogueQuery {
    fun filter(photos: List<PhotoWallpaper>, query: String = "", category: String? = null,
        favouritesOnly: Boolean = false, favourites: Set<String> = emptySet()): List<PhotoWallpaper> {
        val terms = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return photos.filter { photo ->
            (category == null || photo.category == category) &&
                (!favouritesOnly || photo.id in favourites) &&
                terms.all { term ->
                    listOf(photo.title, photo.subject, photo.category, photo.creator, photo.licence,
                        if (photo.synthetic) "generated" else "photograph").any { it.contains(term, ignoreCase = true) }
                }
        }
    }
}

