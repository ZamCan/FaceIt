# FaceIt — Android Architecture
## Platform baseline
Native Android using Kotlin and Jetpack Compose; min SDK 26, compile SDK 35 and target SDK 36. Feature-first packages isolate platform adapters from presentation. Core use is local-first. There is no input-method service: keyboard appearance is an in-app design gallery (AGENTS.md §10), so nothing in the manifest registers an `InputMethodService`.
## Runtime layers
1. Presentation: Compose screens, navigation and accessibility.
2. Domain: composition model, layer rules and feature policy.
3. Data: local preferences, catalogue manifest, asset metadata and future Room collections.
4. Platform adapters: WallpaperManager, ACTION_DIAL, AppWidgetProvider, role-gated dialer integration and optional Android HOME role (RoleManager on Android 10+, Home settings fallback on earlier versions).
5. Resource coordination: lifecycle, memory, thermal, battery and cache policy.
## Feature boundaries
Wallpaper catalogue/preview/crop/apply; independent composition layers; optional Face It Home launcher (explicit ROLE_HOME opt-in, installed-app enumeration and launch); launcher icon assets (not universal third-party icon replacement); native widgets; dialer keypad; keyboard design gallery (visual presets only, no system keyboard); Android Keystore-backed local Vault storage; offline content; adaptive resource coordinator.

## Presentation layer

- `ui/GlassSystem.kt` owns the shared material vocabulary (`FaceItGlass`, `GlassTone`) and the runtime `MotionPolicy` that reads system animation scale, battery saver and thermal status. Every card goes through it, so depth, borders and press states cannot drift between screens.
- `ui/theme/Tokens.kt` owns colour for both light and dark schemes, `ui/theme/Typography.kt` the editorial type scale and `ui/theme/Shapes.kt` the geometry.
- `keyboard/KeyboardDesignCatalogue.kt` is pure data plus deterministic rules, which is what makes the palette contrast and migration behaviour unit-testable without a device.

## Identity

`brand/generate_mark.py` is the single source of the Face It mark. It emits the PNG master, the Play Store icon, the monochrome layer, the adaptive foreground/background vectors and every legacy density from one geometry, so icon revisions are reproducible rather than destructive.
## Android integration truth
The current in-app keypad hands a number to Android with ACTION_DIAL. It does not replace the manufacturer's Phone app. A full default dialer requires the dialer role, required components, permissions, validation and explicit user choice. Face It Home is a separate optional launcher integration: `FaceItHomeActivity` requests `RoleManager.ROLE_HOME` through Android's consent UI on API 29+, falls back to Home settings when necessary, enumerates launcher activities through the declared package-visibility query and launches the selected activity. It does not silently become the default Home app. Widgets use native AppWidget routes; the Face It Home grid does not universally replace icons in other launchers. Face It does not install or register a system keyboard.
## Offline data
Catalogue metadata and saved compositions are local. Artwork uses stable IDs. The bundled artwork manifest records dimensions, category, creator, licence, provenance and SHA-256; the active inventory is `photos/photo_index.json`; historical drawn-pack manifests are not shipped inventory. Network updates are optional.
## Verification
Run `gradle testDebugUnitTest assembleDebug` with JDK 17 and Gradle 8.9, plus `python3 tools/fetch_photo_pack.py --verify`, `python3 tools/verify_catalogue.py` and `python3 tools/verify_sources.py`. CI does not replace real-device testing. Current status and outstanding gates are recorded in `docs/VERIFICATION.md`.
