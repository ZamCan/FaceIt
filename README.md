# FaceIt

**Personalise Your World.**

FaceIt is a native Android personalisation platform by ZamCan. It lets people build their own visual environments from independent backgrounds, objects and materials, then apply the resulting artwork to supported parts of their phone.

## Product principles

- **Compose, don't impose:** independent visual layers can be combined without fixed category-bound themes.
- **User-controlled:** each personalisation feature is optional and reversible.
- **Verified build:** the content gate, unit tests, Lint and the debug APK are run by GitHub Actions on every push; see [docs/CI.md](docs/CI.md) for the run-by-run record. Nothing is described as release-ready without a device pass.
- **Offline-first:** imported artwork is stored privately on-device. The bundled pack contains licensed photographs and labelled generated JPEGs indexed in `app/src/main/assets/photos/photo_index.json`; core browsing needs no runtime download.
- **Resource-aware:** inactive features must not keep unnecessary rendering or background work alive.
- **Real functionality:** controls are exposed only when their behaviour is implemented.
- **Privacy-first:** local processing by default.
- **Respectful content:** culturally significant cities, sacred architecture, geometric art and architectural heritage are integrated naturally into the first curated collections.
- **Platform honesty:** integrations use supported Android APIs, roles and explicit user choices.

## Working Android foundation

The current main branch includes:
- Native Kotlin and Jetpack Compose application.
- Local composition preferences and a unified Style Studio with selectable in-app themes (including an optional feminine collection), widgets, icon families, app-grid concepts, keyboard styles, controls, charging previews and display treatments.
- Optional **Face It Home** Android launcher: a user-consented HOME-role activity with an installed-app grid, selected bundled wallpaper, selected theme and optional draped display frame. It does not activate until the user explicitly chooses **Set as Home** and approves Android's prompt.
- Face It theme palettes persist locally and actively recolour the app's own interface; Android system UI remains under OS control.
- Optional Niqab-inspired draped app/display frame with six independently selectable colours; available as a preview and inside Face It Home when that launcher is explicitly selected.
- Device image import for PNG, JPEG and WebP into private app storage.
- Independent background and main-object image assignments.
- Live procedural composition preview with category-specific backgrounds and object/material rendering.
- Wallpaper application with explicit Home screen, Lock screen or Both selection.
- Sculpted dialer number-pad styling with ceramic, glass, stone and metallic appearances.
- Number entry and hand-off to Android's system dialler through `ACTION_DIAL`.
- Native clock widget provider.
- Deterministic adaptive rendering policy with unit tests.
- Artwork manifest and rights/provenance validation tooling.
- Adaptive launcher identity with foreground, background and monochrome icon variants.
- Keyboard Designs gallery: ten selectable visual presets across ceramic, frosted glass, natural stone, brushed metal and soft matte families, with an isolated pressable demonstration. This is a visual personalisation studio only — Face It ships no system keyboard (see AGENTS.md §10).
- Private Vault screens for file import, encrypted inventory, authenticated decryption on open, export and deletion; AES-256-GCM storage uses Android Keystore and private no-backup storage.
- SMS composer with local font/colour previews, explicit SMS-app handoff and GSM-7/UCS-2 multipart segment estimation; the original plain-text body is preserved.

The keyboard feature is a design gallery inside Face It. There is no `InputMethodService` in the manifest, no system-keyboard enable step, and selecting a design never changes Gboard, Samsung Keyboard or any third-party keyboard. Design preferences (selected design and favourites) persist on the device, and a returning user's previous appearance choice is migrated rather than reset.

The in-app dialer keypad is not yet a replacement for the phone's default dialler. Face It Home is an optional launcher, not a universal icon-pack mechanism or a reskin of Samsung One UI. Wallpapers are 161 bundled images: 148 real photographs under free licences — gemstones and minerals, diamonds, bracelets and jewellery, ironwork and industrial spaces, metalwork, wearable design and electronics — each credited to its maker on the tile, plus 13 images generated for this app and labelled as generated. The 100 vector-derived wallpapers, object renders and material tiles were removed. The full record is `docs/PHOTO_PACK.md`. On top of any of them, the name engine sets a name you type in one of fifteen metal finishes — see `docs/NAME_ENGINE.md`. The photographs require attribution and, for 88 of them, share-alike terms; the app shows the credit and the licence rather than hiding it, and it refuses to load a photograph whose credit or licence is incomplete. Continued high-end art direction plus physical-device review remain necessary, and no photograph has yet been seen on a real screen.

## Important implementation boundaries

Keyboard Designs are in-app visual presets. A real system text-entry keyboard would require a new product-owner decision plus a separate privacy, platform and accessibility review; nothing in this repository registers one. The Vault has local import/open/export/delete workflows and refuses to silently generate a replacement encryption key if encrypted records remain; biometric gating, a user recovery mechanism and optional encrypted cloud backup are not implemented. SMS transport is plain text: font and colour styling are local editor presentation only and cannot be transmitted as rich text over ordinary SMS. Google Drive backup is not enabled; it requires a separately designed consent, encrypted backup and recovery flow.

## Planned capabilities

- Expanded and further art-directed heritage architecture, football, professional, city and design collections beyond the current photograph and labelled generated-image catalogue.
- Native widget studio and deeper widget configuration.
- Supported icon personalisation.
- Optional default-dialer integration, subject to Android role and telephony requirements.
- Additional keyboard design families once a curated set is art-directed (the current ten presets are complete for the gallery scope).
- On-demand interactive 3D studio.
- Optional encrypted Google Drive backup/restore with explicit consent and a verified recovery design.
- Platform-specific iOS feasibility and native implementation.

Unimplemented modules are not represented as completed functionality in the app.

## Identity

The Face It mark is three stacked composition slabs: the product's own "compose, don't impose" idea rendered as a silhouette rather than a generic gem or robot head. The mark, its adaptive foreground/background/monochrome layers, the Play Store icon and every legacy density are generated from one geometry in `brand/generate_mark.py`. Provenance and licence are recorded in `brand/PROVENANCE.json`.

```bash
python3 brand/generate_mark.py     # regenerate every icon asset
```

## Build

The Android project uses Gradle, Kotlin and Jetpack Compose.

GitHub Actions runs artwork-manifest validation, unit tests and a debug APK build on pushes to `main`. The debug APK is published as the `FaceIt-debug-apk` workflow artifact. CI also builds the minified release variant and retains it as `FaceIt-unsigned-release-apk`; this unsigned candidate is for engineering review and is not a signed production release.

## Project references

- [Product specification](docs/PRODUCT_SPECIFICATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Design system](docs/DESIGN_SYSTEM.md)
- [Adaptive Resource Engine](docs/ADAPTIVE_RESOURCE_ENGINE.md)
- [Artwork pipeline](docs/ASSET_PIPELINE.md)
- [Roadmap](docs/ROADMAP.md)
- [Verification](docs/VERIFICATION.md)
