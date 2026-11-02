package com.zamcan.faceit.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zamcan.faceit.ui.BrandMark
import com.zamcan.faceit.ui.FaceItGlass
import com.zamcan.faceit.ui.FaceItWordmark
import com.zamcan.faceit.ui.GlassTone
import com.zamcan.faceit.ui.MotionPolicy
import com.zamcan.faceit.ui.theme.FaceItTheme
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import com.zamcan.faceit.resource.ThermalPressure
import androidx.compose.runtime.CompositionLocalProvider
import com.zamcan.faceit.ui.LocalMotionPolicy

/**
 * Compose previews for the identity and the shared glass system.
 *
 * These are development aids only. AGENTS.md §19 requires visual review of real
 * screenshots from a built application, so a preview is never accepted as
 * device verification.
 */
@Preview(name = "Brand mark on light", widthDp = 360, heightDp = 200)
@Composable
private fun BrandMarkLight() = BrandPreview(dark = false)

@Preview(name = "Brand mark on dark", widthDp = 360, heightDp = 200)
@Composable
private fun BrandMarkDark() = BrandPreview(dark = true)

@Composable
private fun BrandPreview(dark: Boolean) {
    FaceItTheme(darkTheme = dark) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalFaceItColors.current.backdropTop)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            FaceItWordmark()
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BrandMark(size = 64.dp)
                BrandMark(size = 40.dp)
                BrandMark(size = 28.dp)
                BrandMark(size = 18.dp)
            }
            Text(
                text = "Launcher and wordmark drawn from one geometry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Glass tones", widthDp = 380, heightDp = 420)
@Composable
private fun GlassTones() {
    FaceItTheme(darkTheme = true) {
        CompositionLocalProvider(
            LocalMotionPolicy provides MotionPolicy(
                animationsEnabled = true,
                reason = null,
                thermalPressure = ThermalPressure.NONE
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalFaceItColors.current.backdropTop)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassTone.entries.forEach { tone ->
                    FaceItGlass(tone = tone, modifier = Modifier.width(340.dp)) {
                        Text(
                            text = tone.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Shared surface: tint, edge reflection, press state.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
