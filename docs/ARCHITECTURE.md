# FaceIt — Android Architecture
## Platform baseline
Native Android using Kotlin and Jetpack Compose; min SDK 26, compile/target SDK 35. Feature-first packages isolate platform adapters from presentation. Core use is local-first. The native keyboard service is registered as an Android InputMethodService and remains user-enabled through system settings.
## Runtime layers
1. Presentation: Compose screens, navigation and accessibility.
2. Domain: composition model, layer rules and feature policy.
3. Data: local preferences, catalogue manifest, asset metadata and future Room collections.
4. Platform adapters: WallpaperManager, ACTION_DIAL, AppWidgetProvider, InputMethodService and role-gated dialer integration.
5. Resource coordination: lifecycle, memory, thermal, battery and cache policy.
## Feature boundaries
Wallpaper catalogue/preview/crop/apply; independent composition layers; supported launcher icon assets; native widgets; dialer keypad; separate text-entry IME; Android Keystore-backed local Vault storage; offline content; adaptive resource coordinator.
## Android integration truth
The current in-app keypad hands a number to Android with ACTION_DIAL. It does not replace the manufacturer's Phone app. A full default dialer requires the dialer role, required components, permissions, validation and explicit user choice. Widgets, icon replacement and keyboard installation also use supported platform routes.
## Offline data
Catalogue metadata and saved compositions are local. Artwork uses stable IDs. The future asset manifest records dimensions, category, creator, licence, provenance and SHA-256. Network updates are optional.
## Verification
Run gradle testDebugUnitTest assembleDebug with JDK 17 and Gradle 8.9. CI does not replace real-device testing.
