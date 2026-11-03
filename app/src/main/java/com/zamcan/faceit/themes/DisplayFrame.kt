package com.zamcan.faceit.themes

/**
 * The draped display frame — the "niqab" direction, implemented as shading.
 *
 * Product-owner decision on the concept: any frame shape in any colour, be it
 * on or off. So the frame is split into two independent axes rather than being a
 * single toggle:
 *
 *  - **shape** — [VeilStyle], five drape and fold forms drawn as translucent
 *    curves that frame the app grid.
 *  - **colour** — [FrameColours], the six named drape colours.
 *
 * What is drawn is geometry: curves, arc edges, layered folds. Nothing here
 * depicts a woman, a face or a garment, and the frame is never a mask over
 * content — it sits behind and around the icons, and can be switched off.
 *
 * The name "niqab" is used in the product copy because that is the reference the
 * product owner gave. The drawing itself stays abstract on purpose; a figurative
 * veil over a face is not something this app will render.
 */

data class FrameColour(
    val id: String,
    val label: String,
    val argb: Long,
    /** Reach for the deep end of the colour when the surface behind it is light. */
    val readsOnLight: Boolean
)

object FrameColours {
    /** {\"None\"} is a real choice and the default: the frame is off until asked for. */
    const val NONE_ID = "None"

    val all: List<FrameColour> = listOf(
        FrameColour("Onyx", "Onyx", 0xFF29232B, readsOnLight = true),
        FrameColour("Rose", "Rose", 0xFFB87991, readsOnLight = true),
        FrameColour("Plum", "Plum", 0xFF6E4B68, readsOnLight = true),
        FrameColour("Pearl", "Pearl", 0xFFE9DDE0, readsOnLight = false),
        FrameColour("Sage", "Sage", 0xFF758D7C, readsOnLight = true),
        FrameColour("Sand", "Sand", 0xFFC5AE91, readsOnLight = false)
    )

    fun find(id: String?): FrameColour? = all.firstOrNull { it.id == id }

    val ids: List<String> get() = all.map { it.id }
}

/**
 * A resolved frame: whether to draw, what shape, and in what colour.
 *
 * Resolution order, so the user's explicit colour choice always wins over a
 * theme's suggestion while still allowing a theme to propose one:
 *  1. an explicit user choice, including "None";
 *  2. otherwise the theme's own [FaceTheme.veilStyle] and [FaceTheme.veilColor].
 */
data class ResolvedFrame(
    val style: VeilStyle,
    val colour: Long
) {
    val enabled: Boolean get() = style != VeilStyle.NONE
}

object DisplayFrameResolver {

    fun resolve(userColourId: String?, theme: FaceTheme): ResolvedFrame =
        when {
            userColourId == FrameColours.NONE_ID -> ResolvedFrame(VeilStyle.NONE, theme.veilColor)
            FrameColours.find(userColourId) != null ->
                ResolvedFrame(fallbackShape(theme.veilStyle), FrameColours.find(userColourId)!!.argb)
            else -> ResolvedFrame(theme.veilStyle, theme.veilColor)
        }

    /**
     * Choosing a colour should show a frame even on a theme that had none, so a
     * colour choice implies the theme's own shape — or a soft drape if the theme
     * declared no frame at all.
     */
    private fun fallbackShape(themeShape: VeilStyle): VeilStyle =
        if (themeShape == VeilStyle.NONE) VeilStyle.SOFT_DRAPE else themeShape
}
