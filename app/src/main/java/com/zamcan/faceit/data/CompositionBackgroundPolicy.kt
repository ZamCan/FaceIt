package com.zamcan.faceit.data

/** Explicit source identities prevent imported filenames being looked up as photo ids. */
sealed class CompositionBackground {
    data class Imported(val fileName: String) : CompositionBackground()
    data class Bundled(val id: String) : CompositionBackground()
    data object Procedural : CompositionBackground()
}

object CompositionBackgroundPolicy {
    fun resolve(environment: PersonalEnvironment, themePhotoId: String): CompositionBackground {
        val nameGround = environment.nameGroundId?.takeIf { environment.nameEnabled && it.isNotBlank() }
        if (nameGround != null) return CompositionBackground.Bundled(nameGround)
        environment.selectedArtworkFile?.takeIf { it.isNotBlank() }?.let {
            return CompositionBackground.Imported(it)
        }
        if (environment.useProceduralBackground) return CompositionBackground.Procedural
        val id = environment.curatedPhotoId?.takeIf { it.isNotBlank() } ?: themePhotoId
        return if (id.isBlank()) CompositionBackground.Procedural else CompositionBackground.Bundled(id)
    }
}

