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
CC BY-SA. 5 are images generated for this application, marked `synthetic: true`
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

This validates manifest structure, file existence, PNG headers, wallpaper dimensions, rights metadata and checksums. To enforce the complete release pack:

```bash
python3 tools/fetch_photo_pack.py --verify
```

## What actually ships

The pack is generated, not licensed from third parties, so every file shares one
provenance: Wikimedia Commons photographs under CC0 / CC BY / CC BY-SA, plus generated renders
from the palettes recorded in `brand/PROVENANCE.json`. No photograph, crest, kit,
player, club, brand mark or sacred text is reproduced.

| Location | Count | Purpose |
|---|---|---|
| `app/src/main/assets/wallpapers/` | 100 | 1080×2400 wallpapers, one per catalogue entry |
| `app/src/main/assets/wallpapers/thumbs/` | 100 | Grid thumbnails, decoded instead of the full file when browsing |
| `app/src/main/assets/objects/` | 10 | Object renders usable as a composition layer |
| `app/src/main/assets/textures/` | 8 | Material tiles for the finish picker (marble, travertine, ceramic, natural stone, brushed metal, frosted glass, basalt, sandstone) |
| `app/src/main/res/drawable-nodpi/` | 3 | Widget picker previews, one per clock style |
| `app/src/main/assets/photos/photo_index.json` | 1 | Runtime catalogue read by `PhotoLibrary` |
| `content/artwork/manifest.json` | 1 | Rights, palette, hash and dimensions per file |
| `content/artwork/contact_sheet.png` | 1 | Human review sheet; not shipped |

Collections: heritage 20, sport 12, form 15, nature 15, science 10, technology 10,
civic 5, design 13. Total bundled asset volume is 14.5 MB: wallpapers 9.2 MB,
thumbnails 2.1 MB, material textures 2.6 MB, objects 260 KB, widget previews
16 KB, runtime index 32 KB.

The pack is built from 36 distinct drawing recipes across 41 palettes, and no two
wallpapers share a recipe-and-palette pair. Three guards in the generator enforce
that, because a duplicated image is filler:

- `build_catalogue()` asserts that every `(recipe, palette)` pair is unique and
  that every registered recipe is actually used.
- `main()` hashes each rendered master and asserts that no two files are
  byte-identical.
- A recipe may be reused at most four times; the second, third and fourth uses are
  rotated or mirrored at render time so they compose differently.

## Runtime wiring

- `data/PhotoLibrary.kt` reads `photo_index.json` from assets, decodes
  thumbnails with a computed `inSampleSize` on `Dispatchers.IO`, and keeps them in
  a 48-entry LRU cache released through `trimMemory()`.
- `ui/CuratedGallery.kt` is the browse surface: category chips, adaptive grid, and
  select or apply per wallpaper.
- `graphics/WallpaperRenderer.kt` accepts a curated background, a curated object
  and a material tile, tiling the material with a `BitmapShader` at fixed alpha so
  cost does not scale with wallpaper size.
- `widget/FaceItClockWidgetStyles.kt` maps each clock style to its shipped preview
  drawable, which the app-widget picker and configuration activity use.

## Content rules

- No scraped or unlicensed player, club, brand, photographer or artist material.
- Distinguish photography from reconstruction and generated artwork.
- Record rights and provenance before shipping.
- Verify place names, historical context and any Arabic calligraphy.
- Never invent religious quotations or use sacred text as disposable decoration.
- Do not bundle fake/demo wallpapers to satisfy the 100-image requirement.

## Runtime rules

- Preserve original PNG source assets.
- Generate thumbnails and preview derivatives as build-time or controlled cache outputs.
- Decode only the image currently needed.
- Do not load all artwork into memory.
- Keep cache limits bounded and avoid automatic downloads of future packs.
- Measure actual APK size and memory use on physical devices.
