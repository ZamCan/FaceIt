package com.zamcan.faceit.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.ui.theme.FaceItMaterial
import com.zamcan.faceit.ui.theme.LocalFaceItColors

@Composable
fun SculptedDialKey(
    digit: String,
    letters: String,
    style: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFaceItColors.current
    val material = when (style) {
        "Natural Stone" -> FaceItMaterial.STONE
        "Frosted Glass" -> FaceItMaterial.GLASS
        "Brushed Metal" -> FaceItMaterial.METAL
        else -> FaceItMaterial.CERAMIC
    }
    val top = material.top(colors.isDark)
    val bottom = material.bottom(colors.isDark)
    val ink = material.ink(colors.isDark)
    val highlight = if (colors.isDark) {
        Color.White.copy(alpha = .18f)
    } else {
        Color.White.copy(alpha = .72f)
    }
    val shape = RoundedCornerShape(23.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 9.dp,
        label = "dialKeyElevation"
    )
    val pressOffset by animateDpAsState(
        targetValue = if (pressed) 2.dp else 0.dp,
        label = "dialKeyPress"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) .985f else 1f,
        label = "dialKeyScale"
    )
    val description = if (letters.isNotBlank()) "$digit, $letters" else digit

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { contentDescription = description }
            .shadow(elevation, shape, clip = false)
            .offset(y = pressOffset)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(highlight, top, bottom)))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (colors.isDark) .46f else .9f),
                        Color.Black.copy(alpha = .16f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (pressed) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = .07f))
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                color = ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = ink.copy(alpha = .64f),
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
