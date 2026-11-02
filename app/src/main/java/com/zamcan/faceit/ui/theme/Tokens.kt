package com.zamcan.faceit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * FaceIt design tokens.
 *
 * The palette keeps a warm, material-led neutral foundation (paper, graphite,
 * obsidian) and reserves saturated colour for brand identity, focus and status.
 * Both light and dark schemes are first-class; neither is a tinted version of
 * the other.
 */
data class FaceItColors(
    val isDark: Boolean,
    val violet: Color,
    val mint: Color,
    val peach: Color,
    val ice: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val card: Color,
    val cardRaised: Color,
    val cardSunken: Color,
    val border: Color,
    val backdropTop: Color,
    val backdropBottom: Color,
    val scrim: Color
)

val LocalFaceItColors = staticCompositionLocalOf {
    FaceItColors(
        isDark = true,
        violet = Color(0xFFC3B0FF),
        mint = Color(0xFF76DCCB),
        peach = Color(0xFFF0C4A2),
        ice = Color(0xFF91B9F4),
        success = Color(0xFF6AD8A2),
        warning = Color(0xFFF2C66D),
        danger = Color(0xFFFFB4AB),
        card = Color(0xFF171B22),
        cardRaised = Color(0xFF1E232C),
        cardSunken = Color(0xFF101318),
        border = Color(0xFF2C323D),
        backdropTop = Color(0xFF0E1014),
        backdropBottom = Color(0xFF090A0D),
        scrim = Color(0xB3000000)
    )
}

internal val LightFaceItColors = FaceItColors(
    isDark = false,
    violet = Color(0xFF5A3FD0),
    mint = Color(0xFF0F7A6B),
    peach = Color(0xFF9A5B22),
    ice = Color(0xFF2E5FA8),
    success = Color(0xFF177A4C),
    warning = Color(0xFF8A5A00),
    danger = Color(0xFFB3261E),
    card = Color(0xFFFFFFFF),
    cardRaised = Color(0xFFF2EDE5),
    cardSunken = Color(0xFFEDE7DD),
    border = Color(0xFFE2DBD0),
    backdropTop = Color(0xFFF8F5F0),
    backdropBottom = Color(0xFFF0EBE3),
    scrim = Color(0x66000000)
)

internal val DarkFaceItColors = FaceItColors(
    isDark = true,
    violet = Color(0xFFC3B0FF),
    mint = Color(0xFF76DCCB),
    peach = Color(0xFFF0C4A2),
    ice = Color(0xFF91B9F4),
    success = Color(0xFF6AD8A2),
    warning = Color(0xFFF2C66D),
    danger = Color(0xFFFFB4AB),
    card = Color(0xFF171B22),
    cardRaised = Color(0xFF1E232C),
    cardSunken = Color(0xFF101318),
    border = Color(0xFF2C323D),
    backdropTop = Color(0xFF0E1014),
    backdropBottom = Color(0xFF090A0D),
    scrim = Color(0xB3000000)
)

internal val FaceItLightScheme = lightColorScheme(
    primary = Color(0xFF5A3FD0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9E2FF),
    onPrimaryContainer = Color(0xFF1B0D52),
    secondary = Color(0xFF0F7A6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC9F5EC),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = Color(0xFF9A5B22),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF311300),
    background = Color(0xFFF8F5F0),
    onBackground = Color(0xFF1B191F),
    surface = Color(0xFFFFFCF7),
    onSurface = Color(0xFF1B191F),
    surfaceVariant = Color(0xFFEAE4DA),
    onSurfaceVariant = Color(0xFF4C4740),
    outline = Color(0xFF7C766C),
    outlineVariant = Color(0xFFD8D1C7),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF302E33),
    inverseOnSurface = Color(0xFFF3EFF4),
    inversePrimary = Color(0xFFC3B0FF),
    scrim = Color(0xFF000000)
)

internal val FaceItDarkScheme = darkColorScheme(
    primary = Color(0xFFC3B0FF),
    onPrimary = Color(0xFF2A1A66),
    primaryContainer = Color(0xFF3F2B8A),
    onPrimaryContainer = Color(0xFFE9E2FF),
    secondary = Color(0xFF76DCCB),
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005146),
    onSecondaryContainer = Color(0xFFC9F5EC),
    tertiary = Color(0xFFF0C4A2),
    onTertiary = Color(0xFF47290F),
    tertiaryContainer = Color(0xFF653E1C),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF0E1014),
    onBackground = Color(0xFFF1EFF6),
    surface = Color(0xFF14171D),
    onSurface = Color(0xFFF1EFF6),
    surfaceVariant = Color(0xFF252A34),
    onSurfaceVariant = Color(0xFFB4AFBE),
    outline = Color(0xFF8D8796),
    outlineVariant = Color(0xFF343A46),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE5E1E9),
    inverseOnSurface = Color(0xFF302E33),
    inversePrimary = Color(0xFF5A3FD0),
    scrim = Color(0xFF000000)
)

/** Material finishes shared by the dialer keypad, keyboard preview and studio cards. */
enum class FaceItMaterial(val label: String, val lightTop: Color, val lightBottom: Color, val darkTop: Color, val darkBottom: Color, val lightInk: Color, val darkInk: Color) {
    CERAMIC(
        label = "Ceramic",
        lightTop = Color(0xFFFFFDF7),
        lightBottom = Color(0xFFD5CBBB),
        darkTop = Color(0xFFE9E1D3),
        darkBottom = Color(0xFFA89C8A),
        lightInk = Color(0xFF2A2926),
        darkInk = Color(0xFF24221F)
    ),
    GLASS(
        label = "Frosted glass",
        lightTop = Color(0xFFEAF5F6),
        lightBottom = Color(0xFF9FB9BE),
        darkTop = Color(0xFFB4CDD2),
        darkBottom = Color(0xFF4C6873),
        lightInk = Color(0xFF173037),
        darkInk = Color(0xFFF2FEFF)
    ),
    STONE(
        label = "Natural stone",
        lightTop = Color(0xFFCDBFA9),
        lightBottom = Color(0xFF8C7C65),
        darkTop = Color(0xFFB5A58D),
        darkBottom = Color(0xFF5E5549),
        lightInk = Color(0xFF1F1B16),
        darkInk = Color(0xFFF7F5F0)
    ),
    METAL(
        label = "Brushed metal",
        lightTop = Color(0xFFE3E8EC),
        lightBottom = Color(0xFF8B949D),
        darkTop = Color(0xFF929AA3),
        darkBottom = Color(0xFF343A42),
        lightInk = Color(0xFF161B20),
        darkInk = Color(0xFFF7F9FB)
    ),
    MATTE(
        label = "Soft matte",
        lightTop = Color(0xFF4A4F59),
        lightBottom = Color(0xFF2B3038),
        darkTop = Color(0xFF4A505B),
        darkBottom = Color(0xFF2A2F38),
        lightInk = Color(0xFFF5F3EF),
        darkInk = Color(0xFFF5F3EF)
    );

    fun top(isDark: Boolean) = if (isDark) darkTop else lightTop
    fun bottom(isDark: Boolean) = if (isDark) darkBottom else lightBottom
    fun ink(isDark: Boolean) = if (isDark) darkInk else lightInk
}

object FaceItSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
