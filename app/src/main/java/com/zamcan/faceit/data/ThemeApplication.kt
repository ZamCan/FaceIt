package com.zamcan.faceit.data

import com.zamcan.faceit.themes.FaceTheme

/**
 * Whether selecting a theme also swaps the wallpaper.
 *
 * A user who has curated a background by hand keeps it by choosing KEEP; a user
 * who wants the whole look in one tap leaves it on FOLLOW_THEME. This is the
 * "compose, don't impose" rule from AGENTS.md §1 expressed as one setting.
 */
enum class ThemeWallpaperMode { FOLLOW_THEME, KEEP }

/**
 * Applies a complete theme while keeping every independent layer independent.
 *
 * Only the fields a theme actually owns are written. Imported user artwork is
 * never touched, and the curated object, material and composition layers survive
 * a theme change — a theme is a starting point, not a reset.
 */
fun PersonalEnvironment.applyingTheme(
    theme: FaceTheme,
    keepWallpaper: Boolean
): PersonalEnvironment = copy(
    themeId = theme.id,
    name = theme.name,
    keypadDesignId = keypadDesignIdFor(theme),
    wallpaperMode = if (keepWallpaper) ThemeWallpaperMode.KEEP else ThemeWallpaperMode.FOLLOW_THEME,
    curatedArtworkId = when {
        keepWallpaper -> curatedArtworkId
        else -> theme.wallpaperId
    }
)

/** The keypad preset that belongs to a theme's look. */
private fun keypadDesignIdFor(theme: FaceTheme): String = when (theme.family) {
    "Ladies" -> "bloom_pad"
    "Cute" -> "pebble_pad"
    "Heritage" -> "stone_pad"
    "Sport" -> "titanium_pad"
    "Abstract" -> "night_pad"
    "Nature" -> "frosted_pad"
    "Calm" -> if (theme.isDark) "frosted_pad" else "paper_pad"
    else -> "ceramic_sculpt"
}
