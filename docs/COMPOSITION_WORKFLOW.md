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

