# FaceIt — Production Roadmap
## Delivered: identity and quality foundation (P0)

- Original Face It mark: three stacked composition slabs, generated from one
  geometry into master PNG, adaptive foreground/background/monochrome vectors,
  Play Store icon and five legacy densities, with provenance recorded.
- Shared glass surface system (`FaceItGlass`) used by every product card, with
  resting, raised, accent and press states.
- Runtime motion policy: system animation scale, battery saver and thermal status
  reduce or disable animation instead of being ignored.
- Splash identity on API 31+ uses the same adaptive mark as the launcher.
- Keyboard Designs gallery replaces the input-method studio, per AGENTS.md §10:
  ten presets across five families, isolated pressable demo, persisted selection
  and favourites, and migration of the previous appearance choice.
- Accessibility: legend and accent contrast is now asserted in unit tests; the
  Pearl Mint accent was corrected after failing the 3.0 non-text threshold.

## Foundation iteration
Native Android project, CI, local composition persistence, functional dialer keypad visual studio with ACTION_DIAL, procedural offline wallpaper renderer and offline visual taxonomy.
## Artwork and wallpaper release gate
Produce and verify at least 100 original or licensed high-quality PNG wallpapers. Build manifest, hashes, thumbnails and responsive derivatives. Implement catalogue search, preview, crop, favourites and offline apply. Verify provenance and licences.
## Platform engines
Native widgets; launcher-compatible icon integration; full dialer role integration after platform review; expanded in-app keyboard design gallery and isolated demonstrations (no system keyboard); encrypted Vault; on-demand 3D and resource lifecycle.
## Release gate
No feature is complete because a screen exists. Each requires behaviour, tests, permission handling, offline/failure handling, accessibility and physical-device validation.
