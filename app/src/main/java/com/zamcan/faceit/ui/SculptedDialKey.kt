package com.zamcan.faceit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SculptedDialKey(
    digit: String,
    letters: String,
    style: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(23.dp)
    val ceramic = style == "Ceramic Sculpt"
    val stone = style == "Natural Stone"
    val metal = style == "Brushed Metal"
    val colors = when {
        ceramic -> listOf(Color(0xFFFFFDF7), Color(0xFFE7E0D3), Color(0xFFC9C0B1))
        stone -> listOf(Color(0xFFB5A58D), Color(0xFF82745F), Color(0xFF5E5549))
        metal -> listOf(Color(0xFF929AA3), Color(0xFF58616B), Color(0xFF343A42))
        else -> listOf(Color(0xFF737F8A), Color(0xFF3E4853), Color(0xFF242C35))
    }
    val ink = if (ceramic) Color(0xFF292A2D) else Color(0xFFF7F5F0)
    Box(
        modifier
            .shadow(9.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(colors))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = if (ceramic) .88f else .42f), Color.Black.copy(alpha = .18f))
                ),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(digit, color = ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            if (letters.isNotEmpty()) {
                Text(
                    letters,
                    color = ink.copy(alpha = .68f),
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
