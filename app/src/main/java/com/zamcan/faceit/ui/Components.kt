package com.zamcan.faceit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.ui.theme.FaceItSpacing
import com.zamcan.faceit.ui.theme.LocalFaceItColors

/**
 * Shared visual primitives for the FaceIt workspace.
 *
 * These components intentionally stay close to Material 3 so the product keeps
 * native Android behaviour, focus handling and accessibility semantics.
 */

@Composable
fun FaceItBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalFaceItColors.current
    Box(
        modifier = modifier
            .background(colors.backdropTop)
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(colors.backdropTop, colors.backdropBottom)
                    )
                )
                val glowRadius = size.width * .85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.violet.copy(alpha = .13f), Color.Transparent),
                        center = Offset(size.width * .96f, size.height * .02f),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(size.width * .96f, size.height * .02f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.mint.copy(alpha = .07f), Color.Transparent),
                        center = Offset(size.width * .02f, size.height * .72f),
                        radius = glowRadius * .8f
                    ),
                    radius = glowRadius * .8f,
                    center = Offset(size.width * .02f, size.height * .72f)
                )
            },
        content = content
    )
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 30.dp) {
    val colors = LocalFaceItColors.current
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Face It" }
    ) {
        val w = this.size.width
        val h = this.size.height
        val halfWidth = w * 0.40f
        val halfHeight = halfWidth * 0.507f
        val wall = h * 0.055f
        val gap = h * 0.235f
        val cx = w * 0.5f
        val cy = h * 0.5f - h * 0.02f

        fun slabPolygons(centreY: Float): Triple<List<Offset>, List<Offset>, List<Offset>> {
            val top = listOf(
                Offset(cx, centreY - halfHeight),
                Offset(cx + halfWidth, centreY),
                Offset(cx, centreY + halfHeight),
                Offset(cx - halfWidth, centreY)
            )
            val leftWall = listOf(
                top[3], top[2],
                Offset(top[2].x, top[2].y + wall),
                Offset(top[3].x, top[3].y + wall)
            )
            val rightWall = listOf(
                top[2], top[1],
                Offset(top[1].x, top[1].y + wall),
                Offset(top[2].x, top[2].y + wall)
            )
            return Triple(top, leftWall, rightWall)
        }

        fun drawSlab(centreY: Float, topStart: Color, topEnd: Color, wallColour: Color, wallDeep: Color) {
            val (top, leftWall, rightWall) = slabPolygons(centreY)
            drawPath(Path().apply {
                moveTo(leftWall[0].x, leftWall[0].y)
                leftWall.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }, wallColour)
            drawPath(Path().apply {
                moveTo(rightWall[0].x, rightWall[0].y)
                rightWall.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }, wallDeep)
            drawPath(
                path = Path().apply {
                    moveTo(top[0].x, top[0].y)
                    top.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                },
                brush = Brush.linearGradient(
                    colors = listOf(topStart, topEnd),
                    start = Offset(top[0].x, top[0].y),
                    end = Offset(top[2].x, top[2].y)
                )
            )
        }

        // base slab, accent slab, lit surface slab — back to front
        drawSlab(cy + gap, colors.cardRaised, colors.cardSunken, colors.cardSunken, colors.cardSunken)
        drawSlab(
            cy,
            colors.violet.copy(alpha = .95f),
            colors.violet.copy(alpha = .62f),
            colors.violet.copy(alpha = .55f),
            colors.violet.copy(alpha = .38f)
        )
        drawSlab(
            cy - gap,
            if (colors.isDark) Color(0xFFF7F4FD) else Color(0xFFFFFFFF),
            if (colors.isDark) Color(0xFFCFC6E4) else Color(0xFFDCD3EC),
            colors.mint,
            colors.mint.copy(alpha = .62f)
        )
    }
}

@Composable
fun FaceItWordmark(
    modifier: Modifier = Modifier,
    subtitle: String = "PERSONALISE YOUR WORLD"
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(size = 28.dp)
        Spacer(Modifier.width(FaceItSpacing.sm))
        Column {
            Text(
                text = "FACEIT",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    description: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = LocalFaceItColors.current.card,
                border = BorderStroke(1.dp, LocalFaceItColors.current.border),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                    color = LocalFaceItColors.current.violet
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (description != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(FaceItSpacing.sm))
            trailing()
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (supporting != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(50),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalFaceItColors.current.violet
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = LocalFaceItColors.current.violet
                    )
                }
            }
        }
    }
}

/**
 * Every product card is a glass surface.
 *
 * Kept as a named adapter so call sites stay readable, but the material,
 * border, highlight and interaction state all come from [FaceItGlass] — one
 * token set, no per-screen drift (AGENTS.md §4).
 */
@Composable
fun FaceItCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = FaceItSpacing.md,
    tone: GlassTone = GlassTone.Standard,
    content: @Composable () -> Unit
) {
    FaceItGlass(
        modifier = modifier.fillMaxWidth(),
        tone = tone,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick,
        contentPadding = contentPadding
    ) {
        content()
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = LocalFaceItColors.current.violet
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = .12f),
        contentColor = tint,
        border = BorderStroke(1.dp, tint.copy(alpha = .24f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InfoNote(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Info,
    tint: Color = LocalFaceItColors.current.violet
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = tint.copy(alpha = .07f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceChips(
    title: String,
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onSelected: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (supporting != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val active = option.equals(selected, ignoreCase = true)
                FilterChip(
                    selected = active,
                    onClick = { onSelected(option) },
                    shape = RoundedCornerShape(50),
                    label = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = if (active) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = LocalFaceItColors.current.card,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = LocalFaceItColors.current.violet.copy(alpha = .14f),
                        selectedLabelColor = LocalFaceItColors.current.violet,
                        selectedLeadingIconColor = LocalFaceItColors.current.violet
                    )
                )
            }
        }
    }
}

@Composable
fun <T> SegmentedChoice(
    options: List<Pair<T, String>>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelected: (T) -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colors.cardSunken,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(Modifier.padding(4.dp)) {
            options.forEach { (value, label) ->
                val active = value == selected
                Surface(
                    onClick = { onSelected(value) },
                    shape = RoundedCornerShape(50),
                    color = if (active) colors.card else Color.Transparent,
                    border = if (active) BorderStroke(1.dp, colors.border) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LayerRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalFaceItColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.padding(vertical = 2.dp) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(colors.cardRaised, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.violet
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val colors = LocalFaceItColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = FaceItSpacing.lg, vertical = FaceItSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(colors.violet.copy(alpha = .1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.violet,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(FaceItSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(FaceItSpacing.md))
            action()
        }
    }
}

@Composable
fun ToolCard(
    icon: ImageVector,
    title: String,
    description: String,
    meta: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    FaceItCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(colors.violet.copy(alpha = .22f), colors.mint.copy(alpha = .16f))
                        ),
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.violet,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                StatusPill(text = meta, tint = colors.mint)
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
        }
    }
}

@Composable
fun MaterialSwatch(
    materialName: String,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    val material = com.zamcan.faceit.ui.theme.FaceItMaterial.entries
        .firstOrNull { it.name.equals(materialName, ignoreCase = true) }
    val top = material?.top(colors.isDark) ?: colors.cardRaised
    val bottom = material?.bottom(colors.isDark) ?: colors.cardSunken
    Surface(
        onClick = onClick,
        modifier = modifier.widthIn(min = 56.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.violet.copy(alpha = .12f) else colors.card,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) colors.violet.copy(alpha = .55f) else colors.border
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Brush.verticalGradient(listOf(top, bottom)), CircleShape)
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selected) {
                Spacer(Modifier.height(3.dp))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.violet,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun GradientTile(
    category: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = LocalFaceItColors.current
    val palette = when {
        category.contains("Heritage", true) -> listOf(Color(0xFF173A3C), Color(0xFF8C7A5B), Color(0xFF233436))
        category.contains("Football", true) || category.contains("Sport", true) -> listOf(Color(0xFF1B3A34), Color(0xFF4E7A5E), Color(0xFF14211D))
        category.contains("Nature", true) -> listOf(Color(0xFF17333C), Color(0xFF6E8370), Color(0xFF22352D))
        category.contains("Geometry", true) -> listOf(Color(0xFF241D36), Color(0xFF5A4968), Color(0xFF161923))
        category.contains("City", true) -> listOf(Color(0xFF222C3D), Color(0xFF7C6D66), Color(0xFF171B25))
        category.contains("Automotive", true) -> listOf(Color(0xFF1B2028), Color(0xFF525A61), Color(0xFF101318))
        category.contains("Health", true) -> listOf(Color(0xFF173A36), Color(0xFF5D8F84), Color(0xFF132521))
        else -> listOf(Color(0xFF202231), colors.violet.copy(alpha = .5f), Color(0xFF111318))
    }
    Box(
        modifier = modifier.background(Brush.verticalGradient(palette)),
        content = content
    )
}
