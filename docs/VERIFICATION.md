# Face It — Verification

Honest record of what has been checked, how, and what remains unverified.

AGENTS.md §2 and §19 forbid claiming that a build, test, device check or release
succeeded unless the result was actually observed. This file is the place where
that evidence lives.

## Current status

| Area | Status | Evidence |
|---|---|---|
| Artwork pack generation | **Ran, passed** | `python3 tools/generate_artwork.py` → 100 wallpapers, 10 objects, 8 material textures, 3 widget previews |
| Artwork manifest validation (release gate) | **Ran, passed** | `python3 tools/validate_artwork_pack.py --release` → 118 manifest entries, 100 wallpapers, hashes and rights metadata valid |
| Artwork visual review | **Inspected** | Contact sheet (`content/artwork/contact_sheet.png`) reviewed collection by collection. The first accepted build turned out to contain 23 duplicate images (26 entries shared a recipe *and* palette); the cause was fixed, not hidden, and the pack now hashes 100/100 unique files |
| Artwork duplicate gate | **Ran, passed** | `tools/generate_artwork.py` asserts unique recipe/palette pairs, full recipe coverage, a reuse cap of four, and no byte-identical masters: 100 files, 0 duplicates |
| Source structure check | **Ran, passed** | `python3 tools/verify_sources.py` → 30 Kotlin files, 90 declared types; delimiters balanced, brace nesting sound, project-internal imports resolve, `R.*` references all exist |
| Identity generation | **Ran, passed** | `python3 brand/generate_mark.py` → master, Play Store, monochrome, preview sheet, adaptive vectors and 5 legacy densities written |
| Icon visual review | **Inspected** | Preview sheet reviewed at 24/32/48/72/96 px and on light, dark, neutral, circle-mask and squircle-mask surfaces |
| Keyboard palette contrast | **Verified numerically** | WCAG 2.1 ratios computed for all five palettes; Pearl Mint accent was raised from 2.78 to 4.27 after failing the 3.0 threshold |
| Curated pack runtime wiring | **Unverified until compiled** | `assets/artwork_index.json` is emitted by the same tool as the PNGs, so ids, paths and dimensions cannot drift; the Kotlin that reads it has not been compiled |
| Gradle unit tests (`testDebugUnitTest`) | **Ran in CI, passed** | GitHub Actions run 41461101080: 35 tests, 0 failures, 0 errors, 0 skipped. Per-suite table in `docs/CI.md` |
| Debug APK build (`assembleDebug`) | **Ran in CI, passed** | Same run: `BUILD SUCCESSFUL`, 30.2 MB debug APK uploaded as an artifact |
| Android Lint | **Ran in CI** | Same run: 0 errors, 4 warnings, all available-version notices. Earlier runs found real defects, all fixed — see `docs/CI.md` |
| Physical-device testing | **Not run** | No device or emulator is attached to CI; nothing here has been rendered on a screen |

The device row is still a release gate. Build, test and Lint are now observed
facts from CI, not claims: see `docs/CI.md` for the run-by-run record. Nothing in this repository should be
described as release-ready until they are executed and recorded here.

## Commands

```bash
# Content and asset integrity
python3 tools/validate_artwork_pack.py
python3 tools/validate_artwork_pack.py --release   # requires the full 100-wallpaper pack

# Source self-check (structural only — not a compiler)
python3 tools/verify_sources.py

# Identity regeneration (idempotent)
python3 brand/generate_mark.py

# Android build, tests and Lint (JDK 17 + Android SDK 35). In this repository they
# are run by .github/workflows/android-ci.yml on every push to main.
gradle --no-daemon testDebugUnitTest assembleDebug
gradle --no-daemon lintDebug
```

## What `tools/verify_sources.py` does and does not do

It checks:

- Balanced `{}`, `()` and `[]` outside comments and string literals.
- Imports of Face It's own packages resolve to a declaration that exists.
- Every `R.string`, `R.color`, `R.id` and manifest resource reference matches a
  real resource.
- No duplicate top-level non-private function names across files.

It does **not** type-check, resolve overloads, validate Compose modifier order,
or verify behaviour. A pass is necessary but not sufficient.

## Known risks that only a real build can settle

1. Compose API surface — new code uses `animateDpAsState`, `drawWithCache`,
   `LocalLifecycleOwner` (from `compose-ui`) and `RoundedCornerShape(percent =)`.
   These are stable in the pinned Compose BOM, but the build is the authority.
2. `LocalLifecycleOwner` — imported from `androidx.compose.ui.platform` to avoid
   adding `lifecycle-runtime-compose` purely for one symbol. If the project later
   adopts that artefact, this import can move.
3. Icon rendering — the adaptive foreground is vector geometry generated from
   `brand/generate_mark.py`; mask clipping has only been simulated, not observed
   on a launcher.

## Device checklist still outstanding

- Launcher icon mask on Samsung One UI and AOSP (circle, squircle, square).
- Themed/monochrome icon rendering on Android 13+.
- Monochrome layer legibility at notification and settings sizes.
- Keyboard Designs gallery at font scale 1.3 and 2.0, small (320 dp) and
  foldable widths.
- Reduced-motion and battery-saver behaviour: confirm the demo skips press
  animation and the destination crossfade snaps.
