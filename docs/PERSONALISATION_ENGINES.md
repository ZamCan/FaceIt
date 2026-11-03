# FaceIt Personalisation Engines — Production Contract

This document defines the next engineering layer. It distinguishes implemented foundations from release work that still requires integration and device verification.

## Brand and launcher identity
- Keep the geometric crystalline FaceIt mark as the canonical identity.
- Generate adaptive foreground/background and monochrome launcher variants from the same master geometry.
- Verify icon masks on Samsung One UI and AOSP launchers; never rely on a single fixed-square icon crop.
- Keep the in-app mark consistent with the launcher icon and splash identity.

## Wallpaper content engine
- A release wallpaper is a real, bundled, high-resolution PNG with a stable ID, dimensions, SHA-256, category, creator, licence and provenance.
- The release validator must reject missing, duplicate, low-resolution, unlicensed or hash-mismatched entries.
- Target at least 100 curated wallpapers before calling the first content pack complete. No generated placeholders or remote hotlinks count.
- Decode sampled bitmaps off the main thread, bound caches, release evicted bitmaps, and render previews at display size.
- Preserve original artwork; crop and colour adjustments are non-destructive composition settings.

## Keyboard appearance gallery
- Keyboard designs are in-app visual presets only, not a system input method.
- Users browse, select, favourite and try isolated pressable demonstrations.
- Never register an InputMethodService or claim to change Gboard, Samsung Keyboard or other apps.

## Themes, widgets, icons and display concepts
- Save local Face It style presets and keep palette, frame colour and material choices independently editable.
- The current clock widget remains a real AppWidget with per-instance style selection; do not imply that a Style Studio preview silently changes existing widget instances.
- Icon and app-grid galleries are previews unless a supported launcher, icon-pack or pinned-shortcut integration is explicitly implemented.
- Niqab-inspired display frames are optional visual concepts with user-selectable colours; they are not a system overlay or launcher replacement.
- Quick-control and charging treatments are preview concepts until a separately approved Android integration exists. Never bypass Android background activity, overlay or foreground-service restrictions.

## Text and SMS
- Rich visual styling belongs to the local composer and preview.
- Carrier SMS/MMS transport is not a rich-text format. The final handoff must be plain text and must preserve the exact message body.
- Open the user's selected SMS application with an explicit recipient and draft. FaceIt does not silently send messages.
- Character counts and multipart SMS estimates must be based on encoding-aware GSM-7/UCS-2 segmentation, not visual character count alone.

## Icons and widgets
- Android launchers control icon-pack support and replacement. Use supported launcher/icon-pack mechanisms; never claim universal icon replacement.
- Widgets are native AppWidget integrations. Each widget must expose real configuration, resize metadata, accessibility descriptions and bounded update scheduling.
- Keep Android launcher placement and host-controlled resizing intact.

## Local Vault and optional cloud
- Local vault uses Android Keystore AES-GCM encryption and private no-backup storage.
- Never store encryption keys beside ciphertext. Never upload vault content automatically.
- Optional Google Drive backup is a separate, explicit feature with user consent, clear account selection, encrypted backup blobs, restore verification and conflict handling.
- A Drive backup must not be described as end-to-end private unless key management actually ensures Google cannot decrypt the content.
- Vault recovery and key-loss consequences must be explained before enabling the feature.

## Release gates
- Test install/upgrade/uninstall, process death, rotation, low-memory recovery and Android 13–15 permissions.
- Verify on a physical Samsung device: launcher icon masks, wallpaper targets, keyboard switching, widget configuration and SMS handoff.
- Run unit tests, lint, release minification and signed APK verification. CI success alone is not physical-device certification.
