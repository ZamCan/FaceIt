package com.zamcan.faceit.launcher

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.ui.DrapeFrame
import com.zamcan.faceit.ui.StudioCategory
import com.zamcan.faceit.ui.StudioPreferences
import com.zamcan.faceit.ui.TopicCatalogue
import com.zamcan.faceit.ui.resolveStudioFrame
import com.zamcan.faceit.ui.theme.FaceItTheme
import java.util.Locale
import com.zamcan.faceit.data.EnvironmentStore
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One exported launcher activity entry, not merely a package label. */
internal data class FaceItLaunchableApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    /** Pre-decoded at 96 px off the main thread; null if the icon could not be read. */
    val iconBitmap: ImageBitmap?
)

/**
 * Optional Face It Home.
 *
 * This is a real `CATEGORY_HOME` activity, but it is **opt-in**: the app never
 * claims the HOME role itself, and Android owns the choice. If the user prefers
 * their stock launcher, nothing here runs at all. The copy on screen says exactly
 * that, because a launcher that quietly tries to become default is the kind of
 * thing that gets an app uninstalled.
 *
 * What it does:
 *  - lists every exported launchable activity, searchable and sorted;
 *  - launches it by explicit component, with no implicit-intent guessing;
 *  - paints the wallpaper the user chose in Face It, not a fixed one;
 *  - honours the theme's ink colours, the studio's app grid, and the draped frame.
 */
class FaceItHomeActivity : ComponentActivity() {

    private var isDefaultHome by mutableStateOf(false)

    private val homeRoleRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshHomeRole()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshHomeRole()

        val preferences = StudioPreferences(this)
        val environment = EnvironmentStore(this).load()
        val appStyle = TopicCatalogue.find(preferences.selected(StudioCategory.APPS))
        val themeStyle = TopicCatalogue.find(preferences.selected(StudioCategory.THEMES))
        // The environment is the record of what the user actually applied; the studio
        // preference can be stale if they changed theme from somewhere else.
        val theme = FaceThemes.find(environment.themeId).takeIf { it.id == environment.themeId }
            ?: (appStyle ?: themeStyle)?.let { TopicCatalogue.themeFor(it) }
            ?: FaceThemes.all.first()
        val displayStyle = TopicCatalogue.find(preferences.selected(StudioCategory.DISPLAY))
        val frame = resolveStudioFrame(theme, preferences.frameColour(), displayStyle?.veilShape)
        // One rule decides the wallpaper, shared with the rest of the app: an
        // imported file, then the photograph the user picked, then the theme's
        // own photograph. A launcher must paint what the user last chose.
        // The launcher composes exactly what the workspace composes, so the name
        // engine appears on the home screen too — a launcher that quietly dropped
        // it would be showing a different wallpaper from the one the user built.
        val wallpaperKey = "${environment.nameEnabled}|${environment.nameText}|" +
            "${environment.nameFinishId}|${environment.nameScriptId}|${environment.nameSettingId}|" +
            "${environment.nameAnchor}|${environment.namePlaque}|" +
            com.zamcan.faceit.graphics.WallpaperComposition.groundIdFor(environment)

        setContent {
            FaceItTheme {
                val localContext = this@FaceItHomeActivity
                var apps by remember { mutableStateOf<List<FaceItLaunchableApp>>(emptyList()) }
                var loading by remember { mutableStateOf(true) }
                var wallpaper by remember { mutableStateOf<ImageBitmap?>(null) }
                var message by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    apps = withContext(Dispatchers.IO) { loadLaunchableApps() }
                    loading = false
                }
                LaunchedEffect(wallpaperKey) {
                    wallpaper = withContext(Dispatchers.IO) {
                        val composed = com.zamcan.faceit.graphics.WallpaperComposition
                            .compose(localContext, environment, 1080, 2400)
                        composedWallpaper?.takeUnless { it.isRecycled }?.recycle()
                        composedWallpaper = composed
                        composed?.asImageBitmap()
                    }
                }
                FaceItHomeScreen(
                    apps = apps,
                    loading = loading,
                    wallpaper = wallpaper,
                    theme = theme,
                    frameStyle = frame.style,
                    frameColour = frame.colour,
                    isDefaultHome = isDefaultHome,
                    message = message,
                    onLaunch = { app -> launchApp(app) { message = it } },
                    onSetAsHome = ::requestHomeRole,
                    onHomeSettings = ::openHomeSettings,
                    onBack = ::finish
                )
            }
        }
    }

    override fun onDestroy() {
        // The composed wallpaper is a full-screen bitmap; a launcher lives a long
        // time, so release it rather than holding it for the process's lifetime.
        composedWallpaper?.takeUnless { it.isRecycled }?.recycle()
        composedWallpaper = null
        super.onDestroy()
    }

    /** The bitmap behind [wallpaper]; kept so it can be recycled on destroy. */
    private var composedWallpaper: android.graphics.Bitmap? = null

    override fun onResume() {
        super.onResume()
        refreshHomeRole()
    }

    private fun refreshHomeRole() {
        isDefaultHome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true
        } else {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName == packageName
        }
    }

    private fun requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val manager = getSystemService(RoleManager::class.java)
            if (manager != null && manager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                homeRoleRequest.launch(manager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        openHomeSettings()
    }

    private fun openHomeSettings() {
        val settings = Intent(Settings.ACTION_HOME_SETTINGS)
        if (settings.resolveActivity(packageManager) != null) {
            startActivity(settings)
        } else {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    /**
     * Every exported launcher activity on the device.
     *
     * Icons are decoded here, on a background thread, at the size they are drawn.
     * Decoding them inside composition would mean a 96 px bitmap per app allocated
     * during a frame, which is what makes third-party launchers stutter on the
     * first swipe.
     */
    private fun loadLaunchableApps(): List<FaceItLaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolve ->
                val activity = resolve.activityInfo ?: return@mapNotNull null
                val icon = runCatching { resolve.loadIcon(packageManager) }.getOrNull()
                    ?: return@mapNotNull null
                FaceItLaunchableApp(
                    packageName = activity.packageName,
                    activityName = activity.name,
                    label = resolve.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                        .ifBlank { activity.packageName },
                    icon = icon,
                    iconBitmap = runCatching {
                        icon.toBitmap(width = 96, height = 96).asImageBitmap()
                    }.getOrNull()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    /**
     * Launch, and say so if it fails.
     *
     * The previous version wrapped this in runCatching and dropped the failure,
     * which turns "the app could not start" into "the tap did nothing".
     */
    private fun launchApp(app: FaceItLaunchableApp, onFailure: (String) -> Unit) {
        val launch = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(launch) }.onFailure {
            onFailure("${app.label} could not be opened")
        }
    }
}

@Composable
private fun FaceItHomeScreen(
    apps: List<FaceItLaunchableApp>,
    loading: Boolean,
    wallpaper: ImageBitmap?,
    theme: FaceTheme,
    frameStyle: com.zamcan.faceit.themes.VeilStyle,
    frameColour: Long,
    isDefaultHome: Boolean,
    message: String?,
    onLaunch: (FaceItLaunchableApp) -> Unit,
    onSetAsHome: () -> Unit,
    onHomeSettings: () -> Unit,
    onBack: () -> Unit
) {
    val background = Color(theme.background)
    val raised = Color(theme.surfaceRaised).copy(alpha = .94f)
    // Ink comes from the theme, not from white. A light theme with white labels over
    // its own pale wallpaper is the obvious failure, and Face It ships one.
    val ink = Color(theme.textPrimary)
    val secondary = Color(theme.textSecondary)
    val accent = Color(theme.accent)

    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        wallpaper?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        // A scrim, not a tint: labels must stay legible over any wallpaper the user
        // picked, including a near-white one.
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            background.copy(alpha = .78f),
                            background.copy(alpha = .58f),
                            background.copy(alpha = .86f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Header(
                accent = accent,
                ink = ink,
                isDefaultHome = isDefaultHome
            )

            Spacer(Modifier.height(10.dp))

            // A real clock: a home screen without the time is a folder.
            ClockBlock(theme = theme, ink = ink, secondary = secondary, accent = accent)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Search apps", color = secondary) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = secondary)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear search",
                            tint = secondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { query = "" }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = raised,
                    unfocusedContainerColor = raised,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = ink,
                    unfocusedTextColor = ink
                )
            )

            Spacer(Modifier.height(10.dp))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loading -> Text(
                        "Loading apps…",
                        Modifier.align(Alignment.Center),
                        color = secondary
                    )
                    apps.isEmpty() -> Text(
                        "No launchable apps were found on this device.",
                        Modifier.align(Alignment.Center),
                        color = secondary,
                        textAlign = TextAlign.Center
                    )
                    filtered.isEmpty() -> Text(
                        "Nothing matches \"${query.trim()}\".",
                        Modifier.align(Alignment.Center),
                        color = secondary,
                        textAlign = TextAlign.Center
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filtered, key = { "${it.packageName}/${it.activityName}" }) { app ->
                            HomeAppTile(app, ink, onClick = { onLaunch(app) })
                        }
                    }
                }

                if (frameStyle != com.zamcan.faceit.themes.VeilStyle.NONE) {
                    // The frame sits behind the tap targets, never in front of them.
                    DrapeFrame(
                        style = frameStyle,
                        colour = Color(frameColour),
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.55f)
                    )
                }
            }

            message?.let {
                Text(
                    it,
                    Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    color = secondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Surface(color = raised, shape = RoundedCornerShape(22.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isDefaultHome) "Face It is your Home app" else "Try your personal home screen",
                        style = MaterialTheme.typography.titleSmall,
                        color = ink
                    )
                    Text(
                        if (isDefaultHome) {
                            "You can change your Home app at any time in Android settings."
                        } else {
                            "Nothing changes until you choose Set as Home and approve Android's prompt."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = secondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onSetAsHome,
                            enabled = !isDefaultHome,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isDefaultHome) "Home is active" else "Set as Home")
                        }
                        TextButton(onClick = onHomeSettings) { Text("Home settings") }
                    }
                    TextButton(onClick = onBack) { Text("Back to Face It") }
                }
            }
        }
    }
}

@Composable
private fun Header(accent: Color, ink: Color, isDefaultHome: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "FACE IT HOME",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = accent
            )
            Text(
                "Your space",
                style = MaterialTheme.typography.headlineSmall,
                color = ink,
                fontWeight = FontWeight.SemiBold
            )
        }
        Surface(
            color = if (isDefaultHome) accent.copy(alpha = .22f) else Color.Black.copy(alpha = .22f),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                if (isDefaultHome) "ACTIVE HOME" else "OPTIONAL",
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
                color = ink
            )
        }
    }
}

/**
 * The time and date.
 *
 * Ticks once a minute rather than once a second: a home screen that recomposes
 * sixty times a minute shows the same minute sixty times, and this one has to
 * stay cheap when it is the process the user returns to constantly.
 */
@Composable
private fun ClockBlock(theme: FaceTheme, ink: Color, secondary: Color, accent: Color) {
    var now by remember { mutableStateOf(java.time.LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = java.time.LocalDateTime.now()
            val millisToNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
            kotlinx.coroutines.delay(millisToNextMinute)
        }
    }
    val time = remember(now) { "%02d:%02d".format(now.hour, now.minute) }
    val date = remember(now) {
        now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } +
            ", " + now.dayOfMonth + " " +
            now.month.name.lowercase().replaceFirstChar { it.uppercase() }
    }
    Column {
        Text(
            time,
            color = ink,
            fontSize = 46.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.displaySmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(date, color = secondary, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Text("·", color = accent, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Text(theme.name, color = accent, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HomeAppTile(
    app: FaceItLaunchableApp,
    ink: Color,
    onClick: () -> Unit
) {
    // Already decoded off the main thread by loadLaunchableApps. If it could not be
    // read at all, fall back to a themed placeholder rather than a blank square.
    val bitmap = app.iconBitmap
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(ink.copy(alpha = .14f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        app.label.take(1).uppercase(),
                        color = ink,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Text(
                app.label,
                color = ink,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
