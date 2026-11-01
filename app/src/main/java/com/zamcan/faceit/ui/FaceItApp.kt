package com.zamcan.faceit.ui
import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.data.CompositionPresets
import com.zamcan.faceit.data.EnvironmentStore
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.VisualCatalogue
import com.zamcan.faceit.graphics.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class Destination(val label: String) { Discover("Discover"), Create("Create"), Dialer("Dialer"), Library("Library"), Tools("Tools") }

@Composable
fun FaceItApp() {
    val context = LocalContext.current
    val store = remember { EnvironmentStore(context) }
    val artworkRepository = remember { ArtworkRepository(context) }
    var environment by remember { mutableStateOf(store.load()) }
    var destination by remember { mutableStateOf(Destination.Discover) }\n    var toolsPage by remember { mutableStateOf("Home") }
    val scope = rememberCoroutineScope()
    fun update(next: PersonalEnvironment) { environment = next; store.save(next) }
    Scaffold(containerColor = Color(0xFF111318), bottomBar = {
        NavigationBar(containerColor = Color(0xFF191C23)) {
            Destination.entries.forEach { item ->
                val icon = when(item) {
                    Destination.Discover -> Icons.Rounded.Explore
                    Destination.Create -> Icons.Rounded.Tune
                    Destination.Dialer -> Icons.Rounded.Dialpad
                    Destination.Library -> Icons.Rounded.Collections
                    Destination.Tools -> Icons.Rounded.Settings
                }
                NavigationBarItem(selected = destination == item, onClick = { destination = item },
                    icon = { Icon(icon, item.label) }, label = { Text(item.label) })
            }
        }
    }) { padding ->
        when(destination) {
            Destination.Discover -> DiscoverScreen(environment, artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()), artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()), Modifier.padding(padding),
                onCreate = { destination = Destination.Create },
                onApply = { targetFlags ->
                    scope.launch(Dispatchers.IO) {
                        runCatching {
                            val bitmap = WallpaperRenderer.render(1080, 2400, environment, artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()), artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()))
                            try {
                                WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, targetFlags)
                            } finally {
                                bitmap.recycle()
                            }
                        }.onSuccess { launch(Dispatchers.Main) {
                            Toast.makeText(context, "Wallpaper applied", Toast.LENGTH_SHORT).show()
                        } }.onFailure { error -> launch(Dispatchers.Main) {
                            Toast.makeText(context, "Wallpaper error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                        } }
                    }
                })
            Destination.Create -> CompositionScreen(environment, Modifier.padding(padding), ::update)
            Destination.Dialer -> DialerScreen(environment.dialerStyle, Modifier.padding(padding)) {
                update(environment.copy(dialerStyle = it))
            }
            Destination.Tools -> ToolsHubScreen(Modifier.padding(padding),\n                onKeyboard = { destination = Destination.Tools; toolsPage = "Keyboard" },\n                onVault = { destination = Destination.Tools; toolsPage = "Vault" },\n                onSms = { destination = Destination.Tools; toolsPage = "SMS" },\n                page = toolsPage, onBack = { toolsPage = "Home" }\n            )\n            Destination.Library -> ArtworkStudioScreen(
                environment.selectedArtworkFile,
                environment.selectedObjectFile,
                onSelected = { selected, layer ->
                    update(if (layer == "Object") environment.copy(selectedObjectFile = selected.fileName) else environment.copy(selectedArtworkFile = selected.fileName))
                    destination = Destination.Discover
                },
                onClearLayer = { layer ->
                    update(if (layer == "Object") environment.copy(selectedObjectFile = null) else environment.copy(selectedArtworkFile = null))
                }
            )
        }
    }
}

@Composable
private fun DiscoverScreen(env: PersonalEnvironment, artworkFile: java.io.File?, artworkObjectFile: java.io.File?, modifier: Modifier, onCreate: () -> Unit, onApply: (Int) -> Unit) {
    var showApplyDialog by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("FACEIT", color = Color(0xFFE9E8EF), fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                Text("Personalise Your World.", color = Color(0xFFA9AAB4), fontSize = 13.sp)
            }
            Surface(shape = CircleShape, color = Color(0xFF292C35)) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFFA78BFA), modifier = Modifier.padding(11.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Your world, composed.", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Text("Independent visual layers. One personal environment.", color = Color(0xFFAAAEB9), fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        CompositionPreview(env, artworkFile, artworkObjectFile, Modifier.fillMaxWidth().height(330.dp))
        Spacer(Modifier.height(14.dp))
        Text(env.name, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
        Text("${env.background} · ${env.mainObject} · ${env.material}", color = Color(0xFFB7B8C1), fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showApplyDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Wallpaper, null); Spacer(Modifier.width(8.dp)); Text("Apply wallpaper")
            }
            OutlinedButton(onClick = onCreate, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Tune, null); Spacer(Modifier.width(6.dp)); Text("Edit")
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (artworkFile == null && artworkObjectFile == null) "Add your own background or object artwork in Library."
            else "Your imported artwork is stored locally and works offline.",
            color = Color(0xFF9FA2AD), fontSize = 12.sp
        )
    }
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply wallpaper") },
            text = { Text("Choose where FaceIt should apply this composition.") },
            confirmButton = {
                TextButton(onClick = {
                    showApplyDialog = false
                    onApply(WallpaperManager.FLAG_SYSTEM)
                }) { Text("Home screen") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showApplyDialog = false
                        onApply(WallpaperManager.FLAG_LOCK)
                    }) { Text("Lock screen") }
                    TextButton(onClick = {
                        showApplyDialog = false
                        onApply(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }) { Text("Both") }
                }
            }
        )
    }
}


@Composable
private fun CompositionScreen(initial: PersonalEnvironment, modifier: Modifier, onSave: (PersonalEnvironment) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal=20.dp, vertical=18.dp)) {
            Text("Composition studio", color=Color.White, fontSize=26.sp, fontWeight=FontWeight.SemiBold)
            Text("Build with independent layers. Your choices stay editable.", color=Color(0xFFA9AAB4), fontSize=13.sp)
        }
        LazyColumn(Modifier.weight(1f), contentPadding=PaddingValues(horizontal=20.dp, vertical=4.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp)) {
            item {
                ChoiceGroup(
                    "Sample compositions",
                    CompositionPresets.all.map { it.name },
                    value.name
                ) { selected -> value = CompositionPresets.find(selected) ?: value }
            }
            if (value.selectedArtworkFile == null) {
                item { ChoiceGroup("Background", listOf("Football","Islamic architecture","Nature","Geometry","Cityscape","Automotive"), value.background) { value=value.copy(background=it) } }
            } else {
                item { Text("Imported background artwork is active. Change or clear it in Library.", color=Color(0xFFA9AAB4), fontSize=12.sp) }
            }
            if (value.selectedObjectFile == null) {
                item { ChoiceGroup("Main object", listOf("Lion sculpture","Geometric crystal","Stethoscope","Mechanical sculpture","None"), value.mainObject) { value=value.copy(mainObject=it) } }
                if (value.mainObject != "None") {
                    item { ChoiceGroup("Material", listOf("Natural stone","Ceramic","Frosted glass","Brushed metal","Marble"), value.material) { value=value.copy(material=it) } }
                }
            } else {
                item { Text("Imported main-object artwork is active. Change or clear it in Library.", color=Color(0xFFA9AAB4), fontSize=12.sp) }
            }
        }
        Button(onClick={onSave(value)}, modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=12.dp).height(54.dp),
            shape=RoundedCornerShape(18.dp)) { Icon(Icons.Rounded.Save,null); Spacer(Modifier.width(8.dp)); Text("Save my environment") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceGroup(title: String, options: List<String>, selected: String, onSelect: (String)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(title, color=Color(0xFFE7E5EB), fontWeight=FontWeight.SemiBold, fontSize=14.sp)
        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            options.forEach { option -> FilterChip(selected=option==selected,onClick={onSelect(option)},label={Text(option)}) }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialerScreen(style: String, modifier: Modifier, onStyle: (String)->Unit) {
    val context=LocalContext.current
    var number by remember { mutableStateOf("") }
    val digits=listOf("1" to "","2" to "ABC","3" to "DEF","4" to "GHI","5" to "JKL","6" to "MNO",
        "7" to "PQRS","8" to "TUV","9" to "WXYZ","*" to "","0" to "+","#" to "")
    Column(modifier.fillMaxSize().padding(horizontal=22.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("Dialer keypad studio",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.SemiBold)
        Text("Raised, individually sculpted number keys.",color=Color(0xFFA9AAB4),fontSize=13.sp)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(value=number,onValueChange={number=it.filter { c->c.isDigit()||c=='+'||c=='*'||c=='#' }},
            modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Phone number")},
            trailingIcon={IconButton(onClick={number=number.dropLast(1)}){Icon(Icons.Rounded.Backspace,"Delete last digit")}})
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("Ceramic Sculpt","Frosted Glass","Natural Stone","Brushed Metal").forEach {
                FilterChip(selected=style==it,onClick={onStyle(it)},label={Text(it,fontSize=10.sp)})
            }
        }
        Spacer(Modifier.height(14.dp))
        for(row in 0..3) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                for(col in 0..2) {
                    val (digit,letters)=digits[row*3+col]
                    SculptedDialKey(
                        digit = digit,
                        letters = letters,
                        style = style,
                        onClick = { number += digit },
                        modifier = Modifier.weight(1f).height(72.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
            OutlinedButton(onClick={number=""},modifier=Modifier.weight(1f)){Text("Clear")}
            Button(onClick={
                if(number.isNotBlank()) runCatching {
                    context.startActivity(Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",number,null)))
                }.onFailure { Toast.makeText(context,"Unable to open dialler",Toast.LENGTH_SHORT).show() }
            },modifier=Modifier.weight(2f).height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF76DCCB))) {
                Icon(Icons.Rounded.Call,null,tint=Color(0xFF111318)); Spacer(Modifier.width(7.dp)); Text("Open phone dialler",color=Color(0xFF111318))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("This in-app keypad hands the number to Android's system dialler. It does not place calls or replace the default Phone app.",
            color=Color(0xFF8F939F),fontSize=11.sp)
    }
}

@Composable
private fun LibraryScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("Visual universes",color=Color.White,fontSize=26.sp,fontWeight=FontWeight.SemiBold)
            Text("Explore independent collections and combine them freely.",color=Color(0xFFA9AAB4),fontSize=13.sp) }
        items(VisualCatalogue.categories) { category ->
            Surface(color=Color(0xFF20232B),shape=RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(category.title,color=Color.White,fontWeight=FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(category.description,color=Color(0xFFA9AAB4),fontSize=12.sp)
                    }
                    Text(category.collection,color=Color(0xFFA78BFA),fontSize=9.sp,fontWeight=FontWeight.Bold)
                }
            }
        }
        item { Text("Catalogue metadata is offline. Curated photographic wallpaper packs are added through the verified artwork pipeline.",
            color=Color(0xFF8F939F),fontSize=11.sp) }
    }
}
