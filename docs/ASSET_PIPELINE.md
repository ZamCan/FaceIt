# FaceIt Artwork Pipeline

## Two distinct asset flows

### 1. User-imported artwork
The Android app's Artwork Studio accepts PNG, JPEG and WebP through the system document picker. It copies selected files into FaceIt's private application storage, validates the image header and dimensions, records metadata locally and works without network access.

Users can assign an imported image independently as:
- Background artwork.
- Main-object artwork (transparent PNG recommended).

The composition renderer keeps these layers independent. Removing or replacing one layer must not reset the other. Imported files remain private to the app unless the user explicitly exports them.

### 2. The bundled wallpaper pack
Wallpapers are bundled JPEG assets under a single directory:
- `app/src/main/assets/photos/` (masters)
- `app/src/main/assets/photos/thumbs/` (gallery thumbnails)

The drawn artwork pack — `assets/wallpapers/`, `assets/objects/`,
`assets/textures/` and `artwork_index.json` — was removed on instruction. Nothing
is drawn and shipped as a raster any more; the studio draws its objects and
materials procedurally at render time.

Every asset has a record in `photos/photo_index.json` with:
- stable unique id and relative file path;
- display title, subject and category;
- creator, creator URL, licence, licence version and licence URL;
- source page and provenance string;
- SHA-256 checksum and byte length;
- dimensions, both as shipped and as sourced;
- `shareAlike` and `synthetic` flags;
- the quality measurements the file passed.

148 of the records are photographs from Wikimedia Commons under CC0, CC BY or
CC BY-SA. 13 are images generated for this application, marked `synthetic: true`
and dedicated to CC0, because the free-licence pool does not contain the rendered
register the pack was asked for.

The pack is verified by `python3 tools/fetch_photo_pack.py --verify` (files,
hashes, attribution) and audited by `tools/verify_catalogue.py` (licences,
duplicates, orphans, dimensions, generated-image disclosure, theme cross-refs).

## Validation

Run:

```bash
python3 tools/fetch_photo_pack.py --verify
```

This validates the current photograph catalogue, file presence, dimensions,
attribution and checksums. Also run:

```bash
python3 tools/verify_catalogue.py --verbose
python3 tools/verify_sources.py
```

## What actually ships

The active inventory is `app/src/main/assets/photos/photo_index.json`.
Masters are JPEGs in `assets/photos/`; gallery derivatives are in
`assets/photos/thumbs/`. Entries distinguish licensed photographs from
generated images. See `docs/PHOTO_PACK.md` for the curation record.
The old `content/artwork/` manifest and contact sheet are historical; they
do not describe bundled files and must not drive release counts.

## Runtime wiring

- `data/PhotoLibrary.kt` reads the current catalogue and filters incomplete credits.
- `data/PhotoLibraryBitmaps.kt` samples images before decode and bounds its
  thumbnail LRU by allocated bytes. Memory trimming drops cache references;
  it does not recycle images still owned by visible consumers.
- `ui/PhotoGallery.kt` browses the current photo catalogue.
- `graphics/WallpaperRenderer.kt` renders procedural composition layers.
- `widget/FaceItClockWidgetStyles.kt` maps styles to widget previews.

## Content rules

- No scraped or unlicensed player, club, brand, photographer or artist material.
- Distinguish photography from reconstruction and generated artwork.
- Record rights and provenance before shipping.
- Verify place names, historical context and any Arabic calligraphy.
- Never invent religious quotations or use sacred text as disposable decoration.
- Do not bundle fake/demo wallpapers to satisfy the 100-image requirement.

## Runtime rules

- Preserve original bundled JPEG masters and user-imported source files.
- Generate thumbnails and preview derivatives as build-time or controlled cache outputs.
- Decode only the image currently needed.
- Do not load all artwork into memory.
- Keep cache limits bounded and avoid automatic downloads of future packs.
- Measure actual APK size and memory use on physical devices.
