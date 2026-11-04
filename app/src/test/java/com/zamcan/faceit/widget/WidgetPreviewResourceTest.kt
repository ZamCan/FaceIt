package com.zamcan.faceit.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every widget finish must ship a preview, and every preview must be referenced.
 *
 * Two of the three original previews were unreferenced, which Lint reported as
 * UnusedResources and which meant the config screen showed only colour blocks.
 * A finish without a preview is now a test failure rather than a design debt.
 */
class WidgetPreviewResourceTest {

    @Test
    fun everyFinishHasAPreviewResource() {
        FaceItClockWidgetStyles.all.forEach { style ->
            assertTrue("${style.id} has no preview", style.previewRes != 0)
        }
    }

    @Test
    fun previewsAreDistinctPerFinish() {
        val resources = FaceItClockWidgetStyles.all.map { it.previewRes }
        assertEquals(
            "two finishes share a preview drawable",
            resources.size,
            resources.toSet().size
        )
    }

    @Test
    fun thereIsOnePreviewPerFinishOnDisk() {
        // Every finish has its own bundled preview. The generator writes exactly
        // this set, and the two files must be kept in step by hand when either grows.
        assertTrue(
            "only ${FaceItClockWidgetStyles.all.size} finishes",
            FaceItClockWidgetStyles.all.size >= 15
        )
    }
}
