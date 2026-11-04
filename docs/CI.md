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
| 12 | `10a43de` | **PASS** | 35 / 35 | **0 errors, 4 warnings** | Compose warnings fixed. Lint reports only the four available-version notices. |
| 13 | `2bbef5e` | **PASS** | **35 / 35** | **0 errors, 4 warnings** | Head. Re-run of run 12 after the documentation commit; same result, so the numbers above are reproducible, not a one-off. |

**Correction.** I first wrote that run 10 produced 4 warnings. It produced 7: the
three Compose findings were listed as `7 warnings` in the report I had already
downloaded, and I summarised the dependency ones only. Recording the mistake rather
than quietly editing the table.

Run 13 (previous head, `2bbef5e`): `https://github.com/ZamCan/FaceIt/actions/runs/41462203203`.
Run 12, its immediate predecessor, produced identical numbers.
Artifacts: `FaceIt-debug-apk` (30.2 MB), `FaceIt-reports` (JUnit XML, HTML test
report, Lint HTML + TXT). This is the run the numbers below come from.

## Unit test results (runs 12 and 13, from JUnit XML)

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

`lintDebug` on runs 12 and 13: **0 errors, 4 warnings**, all `GradleDependency`.

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

## Run 14 — style studio consolidation and the launcher integration

- Head: `3402220bf4f40a0cd8622c999535e61676f3dfb7` on `main`.
- Actions run: `41672292134` (workflow run #173); job *Content gate, build, unit tests and Lint*: **success**.
- Result: **success**. 112 tests, 0 failures, 0 errors, 0 skipped, 16 suites.
  Lint 0 errors / 4 warnings (all four `GradleDependency` dependency-version notices).
  Debug APK artifact: 30,588,168 bytes (about 30.6 MB).
- Artifacts: `FaceIt-debug-apk` (ID `13249590121`, SHA-256
  `9e54fa178d11a55405ec739863bca8e89b56a54e82d02070fe0cf561f77c557a`) and
  `FaceIt-reports` (ID `13249510373`, SHA-256
  `23ab813580d00fe0134d5382799bf382cf3883cd4e3c6c4222abb115356055ab`).
- This run proves the launcher and style-studio sources compile and pass the
  repository's automated gates. It does **not** prove the HOME role, app grid,
  wallpaper, frame or widget works on a physical Samsung device.
- Two Lint warnings were introduced and removed inside this run: the widget picker
  previews had lost their only reference and were reported as `UnusedResources`.
  Rather than delete honest artwork, the generator now renders all nine finishes and
  each style declares its own `previewRes` again.
- Six CI-only failures were found and fixed across this work. They are listed
  rather than summarised, because each one is a hole in the local checks:
  1. `IconRenderer` routed 22 glyphs through a `stroke` paint that does not exist in
     that file (it is called `paint`).
  2. `PersonalisationScreens` used `Surface` without importing it. `verify_sources.py`
     does not check imports; the compiler does.
  3. `appliedStyleFor` was deleted along with the helper it sat beside.
  4. Android `Color.WHITE` is an `Int` where the charging renderer takes `Long`, and
     `Path.addOval` needs an explicit `Path.Direction`.
  5. One test passed an `Int` where the contrast helper takes `Long`.
  6. The Display shelf was generated but never concatenated into the catalogue, so
     `everyCategoryIsPopulated` failed — the test did its job.
- Also fixed on `main` while integrating: the launcher imported `matchParentSize` as
  a top-level function (it is a `BoxScope` member) and used `sp` unimported. Both
  broke `compileDebugKotlin` on `main` before this run.

---

## Run 15 — visual studio, artwork repolish and the optional Home launcher

- Actions run: `41771790549` on `main`. Job *Content gate, build, unit tests and
  Lint*: **success**.
- Result: **119 tests, 0 failures, 0 errors, 0 skipped, 17 suites.** Lint
  **0 errors / 4 warnings** — the same four `GradleDependency` version notices,
  unchanged and deliberate (a Compose BOM bump changes rendering and cannot be
  validated without a device).
- Artifacts: `FaceIt-debug-apk` (~30.6 MB) and `FaceIt-reports`.

### What this run proves

The Kotlin in `main` compiles, all 119 unit tests pass, Lint has no errors, the
artwork pack validates at `--release`, and the debug APK assembles.

### What it does not prove

Nothing here was run on a device. The optional HOME role, the app grid, the
wallpaper compositing, the draped frame, the nine clock widget finishes and every
charging animation are **built and unit-tested, not seen**. No emulator was
available in this environment and the local build chain was intentionally not
installed.

### Failures found and fixed during this phase

Five CI failures, one per push, each a hole in the local checker rather than in
the design:

1. `IconRenderer` routed 22 new glyphs through a `stroke` paint that file calls
   `paint`.
2. `PersonalisationScreens` used `Surface` without importing it.
3. `appliedStyleFor` was deleted along with the helper composable beside it.
4. `Color.WHITE` is an `Int` where the charging renderer takes `Long`;
   `Path.addOval` needs an explicit `Path.Direction`.
5. A test passed `Int` to the `Long`-based contrast helper.

Then two test-level failures, both in my own new tests:

6. `everyThemeAppearsInTheMainShelf…` — the Themes shelf was empty because every
   feminine-family theme had been filed only under Feminine.
7. `everyCategoryIsPopulated` — the Display shelf was generated but never
   concatenated into the catalogue list.

Each is recorded because the pattern matters: **`tools/verify_sources.py` checks
structure, not types and not imports.** Everything it cannot see, CI sees.

### Coordination note, stated plainly

A second process was committing to this repository during this phase — 38 commits
in one window, including a style studio that overlapped the one under
construction. The merge was done by hand, file by file:

- `tools/generate_artwork.py` — their version became the base, because their
  seven-segment preview renderer is better (font-independent, identical output on
  any machine). The finishing helpers and three rewritten recipes from this side
  were carried over on top.
- `FaceItClockWidgetStyles.kt` — theirs taken wholesale; it is a strict superset
  (same nine finishes, plus `previewRes` and a `detail` line).
- Widget preview PNGs — regenerated from their renderer so the binaries and the
  drawing code agree.
- `VisualStudio.kt` — merged, keeping their lifecycle hardening
  (`resolveStudioFrame`, `shouldPersistStudioSelection`,
  `resolveSelectedStyleId`) and this side's Display shelf and catalogue
  generators.

## Gate 16 — catalogue audit added to CI

The structural checker (`tools/verify_sources.py`) has never caught a catalogue
defect: every red build this project has had passed it first. So a second gate was
added ahead of the compiler, `tools/verify_catalogue.py`, which parses the
catalogues themselves and fails on:

- a catalogue entry whose argument count does not match its data class;
- a duplicate id;
- a theme naming a wallpaper, keyboard design, widget finish, charging style,
  icon tile or frame shape that does not exist;
- a frame shape or charging motif with no renderer, or a renderer route pointing
  at a function that is not there;
- a colour pair below the floor the design system promises (body text 4.5:1,
  secondary and accent 3.0:1, keypad legend 4.5:1, widget time 7:1);
- a widget preview resource missing from `res/drawable-nodpi`, or an orphan
  preview no finish uses;
- documentation quoting counts that have drifted.

It found three real defects on its first run, all pre-existing: the `Warm Paper`,
`Warm Ceramic` and `Pearl Shell` widget finishes had date lines between 4.20:1 and
4.37:1 against their own cards — below the 4.5:1 the widget test claims for body
text. They were lightened to 5.45:1, 5.78:1 and 5.56:1, the generator's preview
colours were corrected to match, and the three preview cards were re-rendered and
verified to contain the new colour. The widget unit test only asserted 3.0:1 for
the date line, which is why nothing had failed before.

The documentation check originally compared a hand-typed table of counts and found
it already stale, so the rule was changed instead: `docs/PERSONALISATION_ENGINES.md`
now defers to the audit for figures, and the audit fails if a hand-typed count
table reappears.

**Status: not yet executed by CI.** The GitHub credential for this session expired
before the commit could be pushed, so this gate is written, run locally (PASS on
all checks) and reviewed, but no CI run has exercised it.

## Runs 16-22 — the catalogue expansion, and what it cost

The product owner asked for as many resources as possible at premium quality. Every
catalogue grew. This is the record of how it went, including the parts that went
wrong, because the run log is only useful if it is honest.

| Run | Commit | Result | What it caught |
|---|---|---|---|
| 16 | 366bc2a | failure | `withAlpha(style.trackColor, ...)` passed a Long where an Int was wanted — twice |
| 17 | 83187e2 | failure | three test failures: `wide_heading` at 0.20em tracking, no keypad with the quick-dial purpose, and an assertion I had broken with a bad patch |
| 18 | 408a297 | failure | the frame test asserted ten shapes; there are nine and an off state |
| 19 | 59bae3f | **success** | 129 tests / 0 / 0 / 0, 18 classes; Lint 0 errors, 4 warnings |
| 20 | 89a9c9c | failure | a test asserted more charging styles than themes, which stopped being true at 42 vs 66 |
| 21 | 2e73b26 | **success** | the same test, rewritten as a ratio that cannot go stale |
| 22 | 34c961b | **success** | 52 writing styles, 36 keypads, all floors held |
| 23 | a02b30b | **success** | documentation pass; 129 tests / 0 / 0 / 0, Lint 0 errors / 4 warnings, 40.8 MB APK |
| 24 | 1388c9a | **success** | widget finishes 15 -> 21, every one reachable from a theme |
| 25 | 8321f3e | **success** | drape shapes 9 -> 13 |
| 26 | d835376 | **success** | themes 66 -> 105; all 100 shipped wallpapers now paired. Verified from the uploaded XML: 129 tests / 0 failures / 0 errors / 0 skipped across 18 classes, Lint 0 errors / 4 warnings |

**Every one of those failures passed the local checkers first.** That is the lesson
of this stretch, and the reason `tools/verify_catalogue.py` now checks constructor
arity, id uniqueness, cross-references, renderer coverage, contrast, resource
presence and entry position — not just that the file parses.

Three defects in existing work came out of it, all fixed:

1. **The studio could not draw 39 of the 52 glyphs.** It fell back to a generic
   rounded square, so the per-family icon vocabularies would have rendered as four
   themes of identical tiles. All 68 glyphs now draw in both the canvas and the
   Compose renderer, and the audit fails if one does not.
2. **All eight icon tile finishes looked identical** apart from corner radius. Each
   now draws its own material signature in the studio.
3. **Three widget finishes had date lines below the contrast floor** they claimed:
   Warm Paper 4.37:1, Warm Ceramic and Pearl Shell 4.20:1, against a 4.5:1 promise.
   Lightened, the generator's preview colours corrected to match, and the three
   preview cards re-rendered.

### Current volume

66 complete themes across ten families (61 of the 100 shipped wallpapers
are now paired with a theme, up from 16), 68 glyphs, 12 tile finishes, 9 drape
shapes plus off, 58 charging styles across 33 motifs, 36 keypads, 52 writing styles,
15 widget finishes with generated previews. Roughly 476 studio cards.

### What is still not verified

- No physical-device review of any of it. Ten frame shapes, twelve tile finishes and
  the per-family icon vocabularies have only been seen as code and as generated art.
- The six new widget finishes have preview cards that match their Kotlin colours by
  construction, but nobody has placed one on a real home screen.
- Seven of the fifteen widget finishes still have no theme pointing at them.
- CI success is not device certification; the release gates in
  `PERSONALISATION_ENGINES.md` are unchanged.
