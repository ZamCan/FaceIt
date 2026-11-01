# FaceIt

**Personalise Your World.**

FaceIt is a native Android personalisation platform by ZamCan. It lets people build their own visual environments from independent backgrounds, objects and materials, then apply the resulting artwork to supported parts of their phone.

## Product principles

- **Compose, don't impose:** independent visual layers can be combined without fixed category-bound themes.
- **User-controlled:** each personalisation feature is optional and reversible.
- **Offline-first:** imported artwork is stored privately on-device. The first curated content release is gated on at least 100 high-quality PNG wallpapers; that pack is not yet complete.
- **Resource-aware:** inactive features must not keep unnecessary rendering or background work alive.
- **Real functionality:** controls are exposed only when their behaviour is implemented.
- **Privacy-first:** local processing by default.
- **Respectful content:** Islamic cities, sacred architecture and Islamic visual heritage lead the first curated collections.
- **Platform honesty:** integrations use supported Android APIs, roles and explicit user choices.

## Working Android foundation

The current main branch includes:
- Native Kotlin and Jetpack Compose application.
- Local composition preferences.
- Device image import for PNG, JPEG and WebP into private app storage.
- Independent background and main-object image assignments.
- Live procedural composition preview with category-specific backgrounds and object/material rendering.
- Wallpaper application with explicit Home screen, Lock screen or Both selection.
- Sculpted dialer number-pad styling with ceramic, glass, stone and metallic appearances.
- Number entry and hand-off to Android's system dialler through `ACTION_DIAL`.
- Native clock widget provider.
- Deterministic adaptive rendering policy with unit tests.
- Artwork manifest and rights/provenance validation tooling.

The in-app dialer keypad is not yet a replacement for the phone's default dialler. The current wallpaper composition renderer provides a procedural foundation; curated photographic artwork and refined production-grade object assets are still required.

## Planned capabilities

- At least 100 bundled, rights-verified PNG wallpapers.
- Expanded curated Islamic architecture, football, professional, nature, city and design collections.
- Native widget studio and deeper widget configuration.
- Supported icon personalisation.
- Optional default-dialer integration, subject to Android role and telephony requirements.
- Separate optional text-entry keyboard.
- On-demand interactive 3D studio.
- Secure local Vault.
- Platform-specific iOS feasibility and native implementation.

Unimplemented modules are not represented as completed functionality in the app.

## Build

The Android project uses Gradle, Kotlin and Jetpack Compose.

GitHub Actions runs artwork-manifest validation, unit tests and a debug APK build on pushes to `main`. The debug APK is published as the `FaceIt-debug-apk` workflow artifact.

## Project references

- [Product specification](docs/PRODUCT_SPECIFICATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Design system](docs/DESIGN_SYSTEM.md)
- [Adaptive Resource Engine](docs/ADAPTIVE_RESOURCE_ENGINE.md)
- [Artwork pipeline](docs/ASSET_PIPELINE.md)
- [Roadmap](docs/ROADMAP.md)
