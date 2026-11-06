# Face It — Verification

Honest record of what has been checked, how, and what remains unverified.

AGENTS.md §2 and §19 forbid claiming that a build, test, device check or release
succeeded unless the result was actually observed. This file is the place where
that evidence lives.

## Current status

| Area | Status | Evidence |
|---|---|---|
| Artwork pack | **Removed** | The 100 vector-derived wallpapers, 10 object renders, 8 material tiles and `artwork_index.json` were deleted on instruction, along with `tools/generate_artwork.py`, `tools/validate_artwork_pack.py` and every reader. A theme that named one of them now names a photograph |
| Artwork manifest validation (release gate) | **Retired** | The validator checked a pack that no longer exists. The photograph pack is audited instead by `verify_catalogue.py`, inside the CI gate |
| Artwork visual review | **Inspected** | Contact sheet (`content/artwork/contact_sheet.png`) reviewed collection by collection. The first accepted build turned out to contain 23 duplicate images (26 entries shared a recipe *and* palette); the cause was fixed, not hidden, and the pack now hashes 100/100 unique files |
| Artwork duplicate gate | **Ran, passed** | `tools/generate_artwork.py` asserts unique recipe/palette pairs, full recipe coverage, a reuse cap of four, and no byte-identical masters: 100 files, 0 duplicates |
| Source structure check | **Ran, passed** | `python3 tools/verify_sources.py` → 30 Kotlin files, 90 declared types; delimiters balanced, brace nesting sound, project-internal imports resolve, `R.*` references all exist |
| Identity generation | **Ran, passed** | `python3 brand/generate_mark.py` → master, Play Store, monochrome, preview sheet, adaptive vectors and 5 legacy densities written |
| Icon visual review | **Inspected** | Preview sheet reviewed at 24/32/48/72/96 px and on light, dark, neutral, circle-mask and squircle-mask surfaces |
| Keyboard palette contrast | **Verified numerically** | WCAG 2.1 ratios computed for all five palettes; Pearl Mint accent was raised from 2.78 to 4.27 after failing the 3.0 threshold |
| Curated pack runtime wiring | **Compiled in CI; device rendering pending** | `assets/artwork_index.json` is emitted by the same tool as the PNGs; Kotlin catalogue and gallery compile in run 173. Full gallery decode/render still needs physical-device review |
| Optional Face It Home launcher | **Compiled in CI; device validation pending** | `FaceItHomeActivity` and manifest HOME intent/role wiring compile in run 173. Role request, app enumeration, app launch, wallpaper/frame rendering and switching back have not been exercised on a physical phone |
| Gradle unit tests (`testDebugUnitTest`) | **Ran in CI, passed** | GitHub Actions run 41672292134 (run 173): 112 tests, 0 failures, 0 errors, 0 skipped. Per-suite output is in the run artifact |
| Debug APK build (`assembleDebug`) | **Ran in CI, passed** | Run 173: build succeeded; debug APK artifact is 30,588,168 bytes (about 30.6 MB) |
| Android Lint | **Ran in CI** | Run 173: 0 errors, 4 warnings, all available-version dependency notices; see `docs/CI.md` |
| Photographic pack sourcing | **Ran, passed** | `python3 tools/fetch_photo_pack.py` against the Wikimedia Commons API: 148 accepted photographs across eight categories, 600+ candidates rejected with recorded reasons, licence classifier verified against NC/ND/GFDL/all-rights-reserved strings |
| Photographic orphan sweep | **Ran, passed** | 408 files with no index entry were found still on disk after three rounds of category removal, costing 87 MB inside the APK. Removed, and `verify_catalogue.py` now fails the build if an orphaned photograph file reappears |
| Photographic pack integrity | **Ran, passed** | `python3 tools/fetch_photo_pack.py --verify` → every photograph has a file, a matching sha256 and full attribution |
| Photographic pack licence audit | **Ran, passed** | `python3 tools/verify_catalogue.py` → 153 items, 52.2 MB, 87 share-alike, 5 generated; every licence shippable, every credit complete, no duplicate hash, no file larger than a phone needs |
| Photographic visual review | **Inspected** | Contact sheets covering every file were reviewed one by one, category by category, across four rounds of curation. Nearly fifty files were removed for being the wrong kind of picture — two Jeff Koons sculptures returned by the animal search, a watercolour, a museum diorama, a satellite composite, industrial gaskets, a CGI watch, a container ship called *Chrystal Emerald*, a group of people named Ruby and Crystal, an AMD Geode processor returned for "geode", a butterfly species called the malachite, and three photographs of surgical stitches returned for "silk tie". Every one of them had passed the numeric quality gates |
| Photograph composition | **Modelled in Python; not rendered on a device** | Centre-cropping a landscape to 9:20 leaves ~576 px of usable width at this pack's resolution, measured across the pack. `PhotoComposer` fits by width over a blurred enlargement of itself instead; the treatment was previewed in Python at quarter scale, not through `android.graphics` on a phone |
| Generated images | **Generated, inspected** | Five images generated for the app, in the register the free-licence pool does not contain: a rose-gold monogram, a black-diamond macro, a backlit fluorite cluster, forged Damascus steel, and a turbine hall. Each is marked `synthetic: true`, credited to "Face It (generated)", dedicated to CC0, and disclosed in `PHOTO_PACK.md`; the audit fails the build if a generated image is unmarked or undisclosed |
| Photographic pack unit tests | **Ran in CI, passed** | Run 41816755114 (`72e0080`): 138 tests, 0 failures, 0 errors, 0 skipped across 19 classes; Lint 0 errors, 4 warnings. Includes a licence-gate test and an end-to-end test over the real bundled index |
| Photographic pack APK cost | **Measured** | Debug APK 117.8 MB with the photograph pack, against 40.8 MB without. The masters were re-encoded from 112 MB of print-sized originals to 63 MB; the remaining size is the photographs themselves |
| Physical-device testing | **Not run** | No physical-device result is recorded; the launcher, wallpaper application, gallery decode path and widget still need hands-on review |

The device row is still a release gate. Build, test and Lint are observed CI facts, not device certification: see `docs/CI.md` for the run-by-run record. Nothing in this repository should be described as release-ready until physical-device checks are executed and recorded here.

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

- Face It Home: approve/decline the HOME role, verify default-home state after resume, enumerate installed apps, launch several apps, and switch back to Samsung One UI Home.
- Face It Home visual review on Samsung One UI: selected wallpaper, app-grid contrast, draped frame visibility, font scaling, touch targets and rotation/process recreation.
- Launcher icon mask on Samsung One UI and AOSP (circle, squircle, square).
- Themed/monochrome icon rendering on Android 13+.
- Monochrome layer legibility at notification and settings sizes.
- Keyboard Designs gallery at font scale 1.3 and 2.0, small (320 dp) and
  foldable widths.
- Reduced-motion and battery-saver behaviour: confirm the demo skips press
  animation and the destination crossfade snaps.
