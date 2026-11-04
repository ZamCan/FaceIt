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
- The current bundled baseline contains 100 unique 1080×2400 generated PNG wallpapers, 100 thumbnails, 10 object renders and 8 material textures. The manifest and release validator verify provenance, dimensions and hashes. Continue art direction and physical full-screen review; do not describe the vector-derived baseline as photographic content or as the end of content development.
- Decode sampled bitmaps off the main thread, bound caches, release evicted bitmaps, and render previews at display size.
- Preserve original artwork; crop and colour adjustments are non-destructive composition settings.

## Keyboard appearance gallery
- Keyboard designs are in-app visual presets only, not a system input method.
- Users browse, select, favourite and try isolated pressable demonstrations.
- Never register an InputMethodService or claim to change Gboard, Samsung Keyboard or other apps.

## Themes, widgets, icons and display concepts
- Save local Face It style presets and keep palette, frame colour and material choices independently editable.
- The current clock widget remains a real AppWidget with per-instance style selection; do not imply that a Style Studio preview silently changes existing widget instances.
- Face It Home is implemented as an optional Android HOME-role launcher. The user must explicitly approve the Android role request; the launcher displays its own installed-app grid, selected bundled wallpaper and optional display frame. Verify role switching and app-launch behaviour on physical devices before release.
- This launcher does not provide universal icon replacement in Samsung One UI or other third-party launchers. Icon-family galleries outside Face It Home remain previews unless a compatible icon-pack or pinned-shortcut integration is separately implemented.
- Niqab-inspired display frames are optional visual treatments. They render in Face It Home only when that launcher is explicitly selected; elsewhere they remain previews.
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

## Catalogue volume — expanded pass (unreleased)

This pass added volume to every catalogue that is drawn in-app, at the product
owner's request to generate as many resources as possible. Nothing here changes
what the app claims to control: the widget finishes are still real AppWidgets,
and everything else is still labelled an in-app preview.

Counts are deliberately **not** printed here. A number typed into a document goes
stale the moment a catalogue grows, and the catalogue audit reports the live figures:

```
python3 tools/verify_catalogue.py --verbose
```

The method behind the volume, which is what this document is for:
How the volume was held to the design bar:
- Every theme, keypad and charging style was checked numerically *before* it was
  written into the source: body text >= 4.5:1 on its surface, secondary text and
  accents >= 3.0:1, keypad legends >= 4.5:1 on the key, charging progress
  >= 2.0:1 on its track. The audit ran as a script; entries that failed were
  recoloured, not shipped.
- Every `wallpaperId`, `keyboardDesignId`, `widgetStyleId` and `chargingStyleId`
  referenced by a theme was resolved against the shipped catalogues, so no theme
  points at art that does not exist.
- The six new widget finishes have six newly rendered preview cards from
  `tools/generate_artwork.py`; their colours are duplicated by hand in that
  generator, which is the one place the two files can drift.
- Unit-test floors were raised for every catalogue, so a lost generator shows up
  as a red build instead of a smaller app.

What is *not* done: none of this has been through CI yet — the run that follows
this commit is the first verification. Physical-device review of the new finishes,
the ten new frame shapes and the per-family icon vocabularies is still outstanding, and
`docs/CI.md` records that as a gate rather than a claim.
