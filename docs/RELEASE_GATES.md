# Face It release gates

The app is still pre-release. A successful build or emulator run is not a
production release decision. See `VERIFICATION.md` for observed evidence and
`COMPOSITION_WORKFLOW.md` / `VAULT_ACCESS.md` for implemented boundaries.

## Current engineering scope

- Shared native Compose/glass workspace, persistent themes and optional Home.
- One composition renderer for editor, workspace, Home and applied wallpaper.
- Independent imported/procedural/bundled sources, object placement, opacity,
  imported crop focus, optional names and independent static scene lighting.
- Named local scene snapshots with restore, rename, duplicate, confirmed deletion,
  session undo/redo and reversible draft reset.
- Offline wallpaper search, categories, persisted favourites, full credit/source
  details and explicit Home/Lock/Both application.
- Native clock widgets, in-app keyboard designs, dial-pad and SMS handoffs.
- Keystore encrypted Vault storage with an app-level device-credential gate;
  no claim of biometric-specific or authentication-bound key protection.
- Event-driven battery, thermal and animation-scale adaptation.

Each item still requires the relevant device, accessibility, failure and performance
checks below. This is a behaviour inventory, not certification of every workflow.

## Required acceptance before production

1. Install and exercise Android API 26 and current Android versions, including a
   low-memory phone and a manufacturer launcher such as Samsung One UI.
2. Validate import cancellation, supported formats, orientation, corrupt/large
   images, missing files, storage pressure, crop/placement, offline operation,
   process death and app upgrades. Check preview versus installed Home/Lock output.
3. Exercise each scene-library action and undo/reset with real imported files.
4. Validate optional HOME consent, app enumeration/launch, rotation and switching
   back to the user's original launcher. Test widget placement, resize and restore.
5. Test Vault credential success/cancellation, missing screen lock, backgrounding,
   process death, key loss, corrupt ciphertext, picker failures and export. Review
   the app-level gate and any future cryptographic key-policy migration separately.
6. Review actual built screenshots in light/dark, large type, narrow/foldable
   layouts and TalkBack. Measure startup, frame time, memory, battery and thermal
   response; verify reduced motion without leaving the screen.
7. Verify asset provenance/attribution, licence display, generated-image disclosure,
   APK footprint, application IDs, versioning, permissions and backup rules.
8. Verify the minified unsigned release build in CI, then provision a protected durable release keystore and configure signing without
   committing secrets, verify the APK signature and upgrade/install behaviour,
   and prepare release notes with known limits. Debug artifacts are not releases.

## Separate unfinished capabilities

- On-demand interactive 3D and its device/resource validation.
- Broader widget families and supported launcher-icon/shortcut integration.
- Full default-dialer functionality and telephony/emergency-call validation.
- Vault recovery/device migration and optional encrypted cloud backup.
- Further art direction and collection expansion with verified rights.
- Native iOS feasibility and implementation.

The cancelled drawn PNG pack and system-keyboard proposal must not be revived.
Charging/control/display concepts remain labelled previews unless a supported,
consented Android integration is actually delivered. Do not replace these
engineering tasks with empty screens or imply system-wide control.
