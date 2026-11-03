# FaceIt Artwork Pipeline

## Two distinct asset flows

### 1. User-imported artwork
The Android app's Artwork Studio accepts PNG, JPEG and WebP through the system document picker. It copies selected files into FaceIt's private application storage, validates the image header and dimensions, records metadata locally and works without network access.

Users can assign an imported image independently as:
- Background artwork.
- Main-object artwork (transparent PNG recommended).

The composition renderer keeps these layers independent. Removing or replacing one layer must not reset the other. Imported files remain private to the app unless the user explicitly exports them.

### 2. Curated bundled artwork
Release wallpaper packs are bundled PNG assets under:
- `app/src/main/assets/wallpapers/`
- `app/src/main/assets/objects/`

Every curated asset must have a manifest record in `content/artwork/manifest.json` with:
- Stable unique ID and relative file path.
- Display title and category.
- Creator/rights holder.
- Licence and source/provenance.
- SHA-256 checksum.
- Asset type (wallpaper or object).

The initial content release requires at least 100 high-quality PNG wallpapers. The
repository now ships exactly that: 100 wallpapers, every one original generated
artwork, verified by `tools/validate_artwork_pack.py --release`.

## Validation

Run:

```bash
python3 tools/validate_artwork_pack.py
```

This validates manifest structure, file existence, PNG headers, wallpaper dimensions, rights metadata and checksums. To enforce the complete release pack:

```bash
python3 tools/validate_artwork_pack.py --release
```

## What actually ships

The pack is generated, not licensed from third parties, so every file shares one
provenance: original vector-derived artwork produced by `tools/generate_artwork.py`
from the palettes recorded in `brand/PROVENANCE.json`. No photograph, crest, kit,
player, club, brand mark or sacred text is reproduced.

| Location | Count | Purpose |
|---|---|---|
| `app/src/main/assets/wallpapers/` | 100 | 1080×2400 wallpapers, one per catalogue entry |
| `app/src/main/assets/wallpapers/thumbs/` | 100 | Grid thumbnails, decoded instead of the full file when browsing |
| `app/src/main/assets/objects/` | 10 | Object renders usable as a composition layer |
| `app/src/main/assets/textures/` | 8 | Material tiles for the finish picker (marble, travertine, ceramic, natural stone, brushed metal, frosted glass, basalt, sandstone) |
| `app/src/main/res/drawable-nodpi/` | 3 | Widget picker previews, one per clock style |
| `app/src/main/assets/artwork_index.json` | 1 | Runtime catalogue read by `CuratedLibrary` |
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

- `data/CuratedLibrary.kt` reads `artwork_index.json` from assets, decodes
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
