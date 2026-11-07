# Composition workflow

The active workspace remains in `faceit_local`; named scene snapshots use the
separate `faceit_saved_scenes` preference file. Existing environments load
without migration or deletion. Up to 100 independent snapshots can be stored.
Create saves a copy and updates the active environment only after the library
write succeeds. Library → Saved scenes supports restore, rename, duplicate and
confirmed deletion. Removing a snapshot keeps imported files and the active
environment. If the collection cannot be parsed, mutation fails rather than
silently replacing it with an empty library.

Create keeps a bounded 50-step undo/redo history for the current editing session.
A new edit after undo discards the redo branch. Scene names are editable.
The active environment and saved snapshots persist; undo history is session-only.

`CompositionBackgroundPolicy` separates imported filenames, bundled IDs and
procedural scenes. An explicitly enabled name ground wins, followed by imported
background, explicit procedural selection, selected photo and theme photo.
Choosing a new photo or procedural scene updates that choice deliberately.
The `useProceduralBackground` field defaults to false for older environments.

`WallpaperComposition` sends resolved sources through `WallpaperRenderer`
once for the editor, workspace, optional Home and applied wallpaper. Previews
display the completed result without laying down the object or name again.
Loaded ground bitmaps are released after rendering; displayed output bitmaps are
released through normal ownership/garbage collection rather than recycling a
bitmap that Compose may still reference.

Unit tests cover source precedence, legacy persistence, independent-layer
snapshots, corrupt/duplicate records, bounded history and undo/redo branching.
The emulator smoke check also saves a scene, restarts the process, duplicates
it and restores it. These checks do not replace physical-phone wallpaper,
storage-pressure or upgrade validation.


## Placement and catalogue controls

Object scale, horizontal/vertical position and opacity are independent saved
fields. Imported backgrounds have independent crop focus. Older environments
keep the original placement defaults; out-of-range stored values are clamped.
A slider drag is one undo step. Resetting object placement keeps the background
and material; centring the crop keeps the object settings.

The wallpaper gallery supports offline multi-term search across title, subject,
category, creator, licence and generated/photograph labels. Category and
favourite filters combine; favourites persist by stable catalogue ID. Details
shows a larger image, full attribution, original source and licence links.
Applying a gallery image explicitly offers Home, Lock or Both. Viewing online
source/licence pages is optional; search, favourites and applying are offline.


## Independent scene lighting

Create offers an optional static scene-lighting pass with five directions,
intensity, softness and cool/warm tint. Two bounded radial shader passes light
and shade the composition before the name is drawn. They do not start a renderer
loop or allocate a blur texture. This is 2D scene illumination, not a claim of
interactive 3D or physically based relighting of a photograph.

Lighting is a separate persisted setting. Older scenes load with it disabled,
so upgrading preserves their previous appearance. Changing or resetting lighting
keeps sources, materials, crop and placement intact. Slider gestures use the same
single-step undo handling as placement controls. Reset draft resets composition
settings without deleting imports or stored scenes; Undo restores the draft.

The editor and Discover fit the complete portrait render inside the preview;
wallpaper verification observes Android's wallpaper IDs instead of depending on
the duration of a success snackbar. Emulator failures retain screenshots,
accessibility hierarchy, wallpaper state and logcat when available.
