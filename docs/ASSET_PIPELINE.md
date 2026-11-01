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

The initial content release requires at least 100 high-quality PNG wallpapers. This is a release gate, not a claim that the current repository already contains 100 curated wallpapers.

## Validation

Run:

```bash
python3 tools/validate_artwork_pack.py
```

This validates manifest structure, file existence, PNG headers, wallpaper dimensions, rights metadata and checksums. To enforce the complete release pack:

```bash
python3 tools/validate_artwork_pack.py --release
```

## Content rules

- No scraped or unlicensed player, club, brand, photographer or artist material.
- Distinguish photography from reconstruction and generated artwork.
- Record rights and provenance before shipping.
- Verify Islamic place names, historical context and any Arabic calligraphy.
- Never invent religious quotations or use sacred text as disposable decoration.
- Do not bundle fake/demo wallpapers to satisfy the 100-image requirement.

## Runtime rules

- Preserve original PNG source assets.
- Generate thumbnails and preview derivatives as build-time or controlled cache outputs.
- Decode only the image currently needed.
- Do not load all artwork into memory.
- Keep cache limits bounded and avoid automatic downloads of future packs.
- Measure actual APK size and memory use on physical devices.
