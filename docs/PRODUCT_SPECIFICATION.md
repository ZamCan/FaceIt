# FaceIt — Product Specification

**Tagline:** Personalise Your World.  
**Product owner/brand:** ZamCan  
**Repository:** ZamCan/FaceIt  
**Stage:** Foundation and specification  
**Initial platform:** Android  
**Product category:** Global mobile personalisation and visual design platform

## 1. Vision and mission

FaceIt enables people to shape their phone's visual environment using original artwork, meaningful objects, materials, lighting, motion and independently configurable interface elements.

The product is not merely a wallpaper downloader, a sports application, a launcher clone or a fixed-theme catalogue. Its defining capability is composition: users construct a personal visual environment from interchangeable layers.

**Core principle: The category is not the theme. The user's combination is the theme.**

## 2. Product principles

- Every major visual category can be combined with other categories.
- Background, object, material, lighting, motion, widgets and icons remain independently editable.
- Users decide which phone experiences FaceIt personalises.
- No forced launcher, dialer, keyboard, SMS app or replacement of system controls.
- Offline core functionality; no mandatory account.
- No huge automatically downloaded libraries or unnecessary background processing.
- No fake functionality, dead buttons or example workflows presented as finished features.
- Accessible, readable and usable on ordinary devices.
- Privacy and permission transparency are mandatory.

## 3. Visual universes and content taxonomy

### First-priority cultural collection
Historic cities, sacred architecture and cultural heritage are a first-priority collection. Initial editorial coverage should include Makkah, Madinah, Al-Quds/Jerusalem, historic mosques, geometric art, architectural ornament, verified calligraphy and seasonal cultural collections such as Ramadan and Eid.

Content rules:
- Verify names, locations, descriptions and historical claims.
- Verify Arabic spelling, calligraphy direction and any religious quotation.
- Never invent Quranic verses, hadith or religious quotations.
- Do not treat sacred text as disposable ornament.
- Distinguish photography, reconstruction and artistic interpretation.
- Record asset ownership, licences, creator and provenance.
- Use respectful composition rules for sacred places.

### Other visual universes
- Football and sporting culture.
- Health and medicine.
- Engineering, architecture and industrial design.
- Law and justice.
- Programming and technology.
- Finance, economics and business.
- Science and research.
- Automotive and transportation.
- Fashion, jewellery and luxury.
- Nature, wildlife and landscapes.
- Geometry, abstract art and 3D objects.
- General cities, travel and world cultures.

Additional cultural and religious collections can be researched and introduced in later updates.

## 4. Personal Visual Environment composition

A composition is a saved configuration of independent layers:

1. Background artwork or scene.
2. Main object or sculpture.
3. Object material.
4. Lighting and atmosphere.
5. Motion level.
6. Widgets.
7. Icon appearance.
8. Optional supported phone-interface personalisations, including dialer keypad.
9. Optional sound and haptic preferences where supported.

Each layer can be changed without resetting unrelated layers. Users can save, duplicate, rename, edit, preview, apply and restore compositions.

### Reference composition: The Stone Guardian

- Background: Football stadium or football-inspired artwork.
- Main object: Lion sculpture.
- Material: Natural stone.
- Motion: Subtle.
- Widgets: Enabled.
- Icons: Enabled.
- Suggested resource profile: Balanced.
- Rendering default: high-quality pre-rendered artwork with lightweight effects; interactive 3D only when explicitly opened.

The lion should feel integrated into the football environment through lighting, scale, shadows and composition. Widgets must remain legible; icons must remain recognisable.

### Reference composition: Madinah Serenity

- Background: Madinah architectural scene.
- Main object: heritage geometric sculpture.
- Material: ivory marble.
- Lighting: warm natural light.
- Motion: subtle or static.
- Widgets and icons: independently configurable.

Sacred-site content must not receive unrelated or inappropriate overlays automatically.

## 5. Wallpaper and offline artwork library

The first release must include at least **100 high-quality PNG wallpapers** as actual bundled offline assets.

Proposed initial allocation (editable during curation):
- Islamic cities and architecture: 20.
- Football and sporting culture: 15.
- Geometry, 3D and abstract: 15.
- Nature, wildlife and landscapes: 15.
- Health, engineering and science: 10.
- Programming and technology: 10.
- Finance, law and business: 5.
- Automotive, fashion and luxury: 10.

Requirements:
- Keep the original PNG artwork in the offline library.
- Generate thumbnails and appropriate preview derivatives.
- Load assets lazily; never decode the entire library into memory.
- Support crop, position, preview, favourite, download/export where applicable and apply through supported wallpaper APIs.
- No automatic download of all future collections.
- Bound cache growth and provide user-controlled cache management.
- Measure actual asset and APK size before setting release-size targets.
- Maintain a content manifest with identifiers, dimensions, category, creator, licence, provenance and file integrity hash.

## 6. Personalisation engines

### Wallpaper Studio
Offline browse, search, category filters, preview, crop, position, favourites and wallpaper application.

### Visual Composition Engine
Combines independent backgrounds, objects, materials, lighting, motion and interface layers. Saves reusable personal environments.

### Theme and Icon Studio
Provides supported icon assets and compatible launcher integration. Must explain launcher/device compatibility and offer graceful fallbacks.

### Widget Studio
Native Android widgets for clock, date, calendar, photos, shortcuts and future content-specific widgets. Widgets use appropriate update intervals and do not run continuous 3D scenes.

### Phone Dialer Keypad Studio
This is the phone **number dial pad**, not the text-entry keyboard. It designs the digits 0–9, star and hash keys, call action and related keypad controls.

Visual styles may include raised separated tablet-like keys, sculpted ceramic, frosted glass, natural stone, metallic and translucent materials. Controls require legible digits, strong touch targets, accessible contrast and clear press feedback.

A custom dialer experience may be delivered through FaceIt's own dialer and/or supported default-phone integration. Reskinning arbitrary manufacturer Phone apps is not assumed. Becoming a default dialer requires user consent and compliance with Android role and permission policies. Emergency calling and core telephony behaviour must never be obstructed.

### Keyboard appearance gallery
A gallery of selectable keyboard visual designs, with previews, favourites and isolated pressable demonstrations inside FaceIt. This is not a system keyboard or an input method: it must not register an `InputMethodService`, intercept typing, request keyboard activation or claim to change Gboard, Samsung Keyboard or other apps. The phone dialer keypad remains a separate feature.

### Interactive 3D Studio
On-demand interactive objects, 360-degree rotation, material and lighting controls, and optional burst/reassembly interactions. App shortcuts on object surfaces are a future supported-experience concept, not an assumed Android system capability.

### Vault
Private local artwork and saved compositions, protected by genuine device authentication or app authentication. A decorative lock screen alone is not security.

### Optional advanced integrations
Launcher, SMS client, sound experiences and other device integrations are independent opt-ins. Each requires separate feasibility, permission and platform-policy review. FaceIt does not claim arbitrary control over Android system buttons, volume keys, notification shade or third-party applications.

## 7. Dialer keypad visual requirements

The raised, separated, medicine-tablet-like button concept is an approved visual direction for the **dialer number pad**:
- Individually separated, rounded or softly pill-shaped key surfaces.
- Subtle elevation and convincing contact shadows.
- Slightly convex/sculpted key tops.
- Material variants: ceramic, frosted glass, natural stone, metallic, matte and translucent.
- Configurable digit typeface, weight, contrast, surface, highlight and press feedback.
- Distinct visual treatment for call, delete, star and hash controls.
- No loss of number readability or familiar dialling interaction.
- Preview in light and dark appearances.
- Respect display scaling, font scaling, touch target and accessibility settings.

## 8. Adaptive Resource Engine

A shared system coordinates all feature engines. It is a core architecture component, not an optional battery-saver screen.

Profiles:
- **Essential:** static artwork, no continuous 3D, minimal effects and services.
- **Balanced:** recommended default; standard transitions and on-demand effects.
- **Immersive:** opt-in advanced 3D, lighting and interactive effects, constrained by device capability.

Requirements:
- Lifecycle-aware rendering and animation.
- Initialise 3D only when required and visible.
- Release textures, framebuffers and GPU resources when finished.
- Adapt image resolution, texture quality and effects to device capability.
- Respond to thermal and battery pressure.
- Avoid unnecessary polling, wake locks and persistent services.
- Use event-driven and appropriately scheduled background work.
- Suspend hidden and unselected experiences.
- Keep preview, thumbnail and full-resolution image paths separate.
- Bound caches and clean expendable previews under storage pressure.
- Do not bundle large future libraries in the APK.
- Do not automatically activate unselected modules.

## 9. Offline-first and network behaviour

Core browsing of bundled artwork, composition editing, local preferences, local favourites, local themes and supported local personalisation must work without network access.

Network features are optional enhancements:
- Future artwork catalogue updates.
- Optional remote content packs.
- Optional backup/sync, only with explicit user choice.
- Optional remote configuration and content publishing.

Network failure must not break the offline library or saved compositions. No mandatory sign-in for core use.

## 10. Privacy, security and permissions

- Request permissions only at the point a user activates the feature that needs them.
- Explain why each permission is needed.
- Keep keyboard content private and local.
- Never use private SMS or Vault content for advertising.
- Encrypt sensitive Vault data and protect access with real authentication.
- Minimise analytics and networking.
- Provide clear data controls and deletion paths.
- Respect Android roles and default-app consent.
- Record and review third-party asset licences.

## 11. Accessibility and quality

- Readable contrast in both light and dark modes.
- Minimum comfortable touch targets and scalable typography.
- Screen-reader semantics and labelled controls.
- Reduced-motion support.
- No essential information communicated only by colour.
- Responsive layouts for different screen sizes and densities.
- Test low-memory, low-battery and thermal conditions.
- No false claim of feature completion based only on a visual screen.

## 12. Commercial principles

- Useful free core with no mandatory account.
- Optional premium original artwork and visual packs may be introduced later.
- Optional nonintrusive advertising only in appropriate discovery areas.
- No ads in dialling, keyboard input, SMS conversations, Vault or essential device functions.
- No deceptive buttons, accidental ad taps or forced reward-video loops.
- Content and infrastructure costs must be measured before scaling.
