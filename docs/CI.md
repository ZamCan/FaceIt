# Face It — CI run log

Append-only record of real GitHub Actions runs for `ZamCan/FaceIt`. Every row here
was read back from the Actions API or from the uploaded reports; nothing in this
file is predicted. A run that failed stays in the table with its cause.

Workflow: `.github/workflows/android-ci.yml`
Job: *Content gate, build, unit tests and Lint* (ubuntu-latest, Temurin JDK 17, Gradle 8.9)

## Runs on 2026-11-02

| # | Commit | Result | Tests | Lint | What it proved or found |
|---|---|---|---|---|---|
| 1 | `efe7c5f` | **FAIL** | — | — | Identity commit. `:app:processDebugResources` failed: 9 × `#FF96FFFFFF` in `faceit_launcher_monochrome.xml` is not a valid colour (ARGB is `#AARRGGBB`). |
| 2 | `fe0f336` | cancelled | — | — | Superseded by the next push in the same concurrency group. |
| 3 | `d859579` | **FAIL** | — | — | Same AAPT colour error, still present. |
| 4 | `9186c16` | **FAIL** | — | — | Artwork de-duplication commit; build still blocked by the colour literals. |
| 5 | `00d0ec5` | **FAIL** | — | — | Colour literals fixed → compile reached Kotlin. `PersonalisationScreens.kt:426` `Unresolved reference 'FontFamily'`. |
| 6 | `4ab8aba` | **PASS** | 35 / 35 | not run | First green build: `testDebugUnitTest` + `assembleDebug`, APK uploaded (30.3 MB). |
| 7 | `d12a110` | **PASS** | 35 / 35 | 0 errors, 22 warnings | CI now records test counts, Lint and artifact sizes. Lint findings listed below. |
| 8 | `b66acdf` | **PASS** | 35 / 35 | 0 errors, 15 warnings | Launcher silhouette, widget overdraw, locale formatting, unused drawable fixed. |
| 9 | `46de583` | **PASS** | 35 / 35 | 0 errors, 8 warnings | Round icon disc, `mipmap-anydpi`, data extraction rules, `targetSdk 36`. |
| 10 | `50e3bff` | **PASS** | 35 / 35 | 0 errors, **7** warnings | Dead launcher rasters removed, round adaptive icon added. 4 dependency notices plus 3 Compose API warnings that only appear once the earlier ones are gone. |
| 11 | `599023a` | **PASS** | 35 / 35 | 0 errors, 7 warnings | Documentation commit. Confirms run 10's result; the three Compose warnings are still open at this point. |
| 12 | `10a43de` | **PASS** | **35 / 35** | **0 errors, 4 warnings** | Compose warnings fixed. Current head. Lint now reports only the four available-version notices. |

**Correction.** I first wrote that run 10 produced 4 warnings. It produced 7: the
three Compose findings were listed as `7 warnings` in the report I had already
downloaded, and I summarised the dependency ones only. Recording the mistake rather
than quietly editing the table.

Run 12 (current): `https://github.com/ZamCan/FaceIt/actions/runs/41461711617`.
Artifacts: `FaceIt-debug-apk` (30.2 MB), `FaceIt-reports` (JUnit XML, HTML test
report, Lint HTML + TXT). This is the run the numbers below come from.

## Unit test results (run 12, from JUnit XML)

| Suite | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|
| `com.zamcan.faceit.PersonalEnvironmentTest` | 2 | 0 | 0 | 0 |
| `com.zamcan.faceit.data.PersonalEnvironmentTest` | 4 | 0 | 0 | 0 |
| `com.zamcan.faceit.keyboard.KeyboardDesignCatalogueTest` | 8 | 0 | 0 | 0 |
| `com.zamcan.faceit.keyboard.KeyboardDesignContrastTest` | 5 | 0 | 0 | 0 |
| `com.zamcan.faceit.keyboard.KeyboardPersonalizationEngineTest` | 3 | 0 | 0 | 0 |
| `com.zamcan.faceit.messaging.SmsSegmentEstimatorTest` | 6 | 0 | 0 | 0 |
| `com.zamcan.faceit.resource.AdaptiveResourcePolicyTest` | 7 | 0 | 0 | 0 |
| **Total** | **35** | **0** | **0** | **0** |

## Lint

`lintDebug` on run 12: **0 errors, 4 warnings**, all `GradleDependency`.

Runs 10 and 11 reported three Compose API findings, fixed in run 12:

- `ArtworkStudioScreen.kt:478` `ModifierParameter`: modifier should be the first
  optional parameter in `CompositionPreview`.
- `Components.kt:211` `ModifierParameter`: same in `ScreenHeader`.
- `SculptedDialKey.kt:84` `UseOfNonLambdaOffsetOverload`: read the animated press
  offset in the layout phase.

The `GradleDependency` notices are:

- `androidx.compose:compose-bom` 2024.09.00 → 2026.09.00 available
- `androidx.activity:activity-compose` 1.9.2 → 1.13.0 available
- `androidx.lifecycle:lifecycle-runtime-ktx` 2.8.6 → 2.11.0 available
- `androidx.core:core-ktx` 1.13.1 → 1.19.1 available

These are deliberately not actioned in a lint-cleanup change: a Compose BOM bump
changes rendering behaviour and cannot be validated without a device.

Warnings cleared across runs 7 → 12, each fixed rather than suppressed:
`IconLauncherShape` (×5) twice for different reasons, `Overdraw`, `DefaultLocale`
(×4), `UnusedResources` (×2), `allowBackup` deprecation, `OldTargetApi`,
`ObsoleteSdkInt`, `DataExtractionRules`, `IconXmlAndPng`.

## What these runs do and do not prove

Proved: the content gate, the Kotlin compiler, the unit test suite, resource
merging, the debug APK assembling, and Lint all succeed on a clean Ubuntu image.

Not proved: anything on a real device or emulator. No instrumentation tests run,
no Compose UI is exercised, the wallpaper apply path, the curated gallery decode
path and the widget are compiled but never rendered. Memory and thermal behaviour
are unmeasured.

## Reproducing

```bash
git clone https://github.com/ZamCan/FaceIt && cd FaceIt
gh workflow run android-ci.yml        # or push to main
gh run watch                          # then: gh run download <id>
```

Locally, the same two cheap gates run without a JDK:

```bash
python3 tools/verify_sources.py
python3 tools/validate_artwork_pack.py --release
```
