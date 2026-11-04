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
import com.zamcan.faceit.data.CuratedLibrary
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.ui.DrapeFrame
import com.zamcan.faceit.ui.StudioCategory
import com.zamcan.faceit.ui.StudioPreferences
import com.zamcan.faceit.ui.TopicCatalogue
import com.zamcan.faceit.ui.resolveStudioFrame
import com.zamcan.faceit.ui.theme.FaceItTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One exported launcher activity entry, not merely a package label. */
internal data class FaceItLaunchableApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable
)

class FaceItHomeActivity : ComponentActivity() {

    private var isDefaultHome by mutableStateOf(false)
    private val curatedLibrary by lazy { CuratedLibrary(applicationContext) }

    private val homeRoleRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshHomeRole()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshHomeRole()

        val preferences = StudioPreferences(this)
        val appStyle = TopicCatalogue.find(preferences.selected(StudioCategory.APPS))
        val themeStyle = TopicCatalogue.find(preferences.selected(StudioCategory.THEMES))
        val theme = (appStyle ?: themeStyle)?.let { TopicCatalogue.themeFor(it) }
            ?: FaceThemes.all.first()
        val displayStyle = TopicCatalogue.find(preferences.selected(StudioCategory.DISPLAY))
        val frame = resolveStudioFrame(theme, preferences.frameColour(), displayStyle?.veilShape)

        setContent {
            FaceItTheme {
                var apps by remember { mutableStateOf<List<FaceItLaunchableApp>>(emptyList()) }
                var loading by remember { mutableStateOf(true) }
                var wallpaper by remember { mutableStateOf<ImageBitmap?>(null) }
                LaunchedEffect(Unit) {
                    apps = withContext(Dispatchers.IO) { loadLaunchableApps() }
                    loading = false
                }
                LaunchedEffect(theme.wallpaperId) {
                    wallpaper = withContext(Dispatchers.IO) {
                        curatedLibrary.find(theme.wallpaperId)?.let { artwork ->
                            curatedLibrary.thumbnail(artwork, 720)?.asImageBitmap()
                        }
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
                    onLaunch = ::launchApp,
                    onSetAsHome = ::requestHomeRole,
                    onHomeSettings = ::openHomeSettings,
                    onBack = ::finish
                )
            }
        }
    }

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

    private fun loadLaunchableApps(): List<FaceItLaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolve ->
                val activity = resolve.activityInfo ?: return@mapNotNull null
                FaceItLaunchableApp(
                    packageName = activity.packageName,
                    activityName = activity.name,
                    label = resolve.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                        .ifBlank { activity.packageName },
                    icon = resolve.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: FaceItLaunchableApp) {
        val launch = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(launch) }
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

    Box(Modifier.fillMaxSize()) {
        wallpaper?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = .62f), background.copy(alpha = .68f))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                color = if (isDefaultHome) accent.copy(alpha = .18f) else raised,
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    if (isDefaultHome) "ACTIVE HOME" else "OPTIONAL HOME",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ink
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (loading) {
                Text(
                    "Loading apps…",
                    Modifier.align(Alignment.Center),
                    color = secondary
                )
            } else if (apps.isEmpty()) {
                Text(
                    "No launchable apps were found.",
                    Modifier.align(Alignment.Center),
                    color = secondary
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        HomeAppTile(app, ink, onClick = { onLaunch(app) })
                    }
                }
            }

            if (frameStyle != com.zamcan.faceit.themes.VeilStyle.NONE) {
                DrapeFrame(
                    style = frameStyle,
                    colour = Color(frameColour),
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Surface(
            color = raised.copy(alpha = .92f),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
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
                Spacer(Modifier.height(8.dp))
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
                    TextButton(onClick = onHomeSettings) {
                        Text("Home settings")
                    }
                }
                TextButton(onClick = onBack) { Text("Back to Face It") }
            }
        }
    }
    }
}

@Composable
private fun HomeAppTile(
    app: FaceItLaunchableApp,
    ink: Color,
    onClick: () -> Unit
) {
    val bitmap = remember(app.packageName, app.icon) {
        app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
    }
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
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                modifier = Modifier.size(48.dp)
            )
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
