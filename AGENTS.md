# AGENTS.md — Face It Product, Design & Engineering Master Directive

> **Repository:** `ZamCan/FaceIt`  
> **Product:** Face It — *Personalise Your World.*  
> **Owner / studio:** ZamCan  
> **Primary platform:** Android  
> **Status:** Living, binding product and engineering directive  
> **Audience:** Every coding agent, designer, contributor, reviewer and automated workflow working in this repository

This file is the first document every agent must read before changing the product. Treat it as the governing brief for product intent, visual quality, architecture, implementation, asset creation and release acceptance. Read the linked specifications as supporting detail, but do not use them to dilute or contradict this directive. If an existing implementation conflicts with this document, inspect it, report the gap and improve it deliberately. Do not silently preserve a poor experience merely because it already exists.

---

## 1. Mission: build a personal visual environment platform

Face It is not a wallpaper gallery with a few decorative settings. It is a premium, practical phone-personalisation and visual-composition platform. People should be able to shape the visual character of their devices: wallpapers, scenes, objects, materials, lighting, widgets, icons, dialer keypad surfaces, selectable keyboard design styles and other supported integrations that Android genuinely permits.

The defining idea is **compose, don't impose**. A user's visual environment is assembled from independent, reusable layers and controls. A background must not dictate the object, material, widget or icon style. Users can create, save, preview, revise, duplicate, apply and restore their own combinations.

The product must feel like a carefully art-directed, professionally engineered consumer product—not a prototype, settings dump, generic AI interface, template marketplace or collection of disconnected demo screens.

### Non-negotiable outcomes

1. The app identity and every first-run screen must look professionally art-directed.
2. Every visual asset shown as finished product content must be genuinely high quality, correctly licensed and correctly integrated.
3. Every visible control must perform its stated action or be clearly identified as unavailable/planned and omitted from production navigation.
4. All major experiences must work on a broad range of Android phones, not only a single flagship device.
5. Core functionality must work offline and must not depend on paid APIs or mandatory accounts.
6. The app must remain responsive, memory-conscious and battery-aware.
7. Visual polish must never conceal incomplete functionality, accessibility defects or platform limitations.

## 2. First-read repository procedure

Before editing:

1. Read this file in full.
2. Read `README.md`, `docs/PRODUCT_SPECIFICATION.md`, `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/ADAPTIVE_RESOURCE_ENGINE.md`, `docs/ASSET_PIPELINE.md` and `docs/ROADMAP.md`.
3. Inspect the actual source tree, current main branch, build scripts, tests, manifests and asset inventory. Do not infer implementation from a screen or document alone.
4. Identify what is implemented, what is partially implemented, what is only a visual mock-up and what is planned.
5. Preserve working features and user data. Make changes incrementally, with tests.
6. Update documentation when behaviour, requirements or limitations change.
7. Never claim a build, test, device test, signing operation or release succeeded unless its result has actually been observed.

Do not create a second competing architecture, duplicate design system or alternate navigation merely to avoid understanding the existing one.

## 3. Brand identity and app icon — highest visual priority

The Face It logo is not a minor finishing detail. It is the first impression of the entire product and must establish the quality bar for every wallpaper, object, widget and control surface.

### Identity direction

Create a distinctive, ownable Face It mark with a premium visual-design language: precise geometry, deliberate negative space, confident silhouette, refined depth and strong small-size recognition. The mark should communicate personal expression, visual composition and crafted design without relying on generic AI symbols, robot heads, chat bubbles, stock gradients or random decorative initials.

The icon must be recognisable at launcher size, notification size, settings size and on both light and dark backgrounds. Avoid excessive detail, thin lines that disappear, illegible lettering and visual noise. Do not scatter unexplained DS or brand glyphs around the home screen.

### Required deliverables

- A professionally finished, high-resolution **PNG master artwork** with clean edges and transparent background where appropriate.
- A square, launcher-safe composition with carefully controlled padding and a clear silhouette.
- Android adaptive icon foreground and background assets, plus monochrome/themed icon artwork where supported.
- Correct density and API-specific launcher resources generated from the master; do not stretch one low-resolution bitmap across all densities.
- A preview sheet showing the mark on light, dark, neutral and launcher-mask backgrounds.
- A source/master file or reproducible generation pipeline so future revisions are not destructive.
- A provenance note confirming that the artwork is original or properly licensed.

Use vector source for scalable geometry where appropriate, but the delivered high-quality PNG master is required. Do not substitute a screenshot, emoji, text-only placeholder, low-resolution icon or an automatically generated default launcher graphic.

### Icon acceptance

Inspect the icon at actual launcher sizes and at 100% zoom. Check silhouette, optical centring, edge quality, contrast, mask clipping, monochrome rendering and consistency across Android launchers. If it looks generic, amateur, crowded or visually weak, it is not ready.

Do not mark the icon task complete until the real resource files are integrated into the Android project and the built APK displays them correctly.

## 4. Visual art direction and design quality

### Overall feeling

Aim for a crafted, premium, tactile and visually engaging product. The interface should feel composed, spacious, deliberate and alive. Use strong visual hierarchy, elegant typography, refined surfaces, meaningful imagery, clear actions and polished transitions.

Use a restrained material vocabulary:
- Refined frosted glass and translucent layers.
- Natural stone, marble and ceramic.
- Brushed metal and satin finishes.
- Soft matte surfaces and tactile raised controls.
- Carefully controlled highlights, ambient shadows and depth.
- Restrained iridescence or spectral accents when they support the artwork.

Materials must look like materials, not a generic blur effect placed behind every card. Use texture and depth with restraint; preserve readability and performance.

### Colour

The brand must not become a monotonous blue or dark-blue interface. Build a coherent neutral foundation with distinctive, controlled accent colours. Different studios may use a recognisable accent identity—such as a warm red, green, amber, violet, mint or another carefully selected hue—without turning the whole app into a rainbow or making every screen a different product.

A light bluish accent may be used selectively in relevant sections, just as a focused product accent, but it must not dominate the entire interface. Establish semantic tokens for surfaces, text, borders, focus, success, warning, error and studio accents. Ensure every token has light and dark values.

Artwork is the visual hero. UI chrome should frame it, not compete with it.

### Glassmorphic visual treatment and premium control design

A refined, tactile glassmorphic language is a major product direction across the app. Use layered translucent surfaces, controlled background diffusion, subtle specular highlights, edge reflections, soft depth, nuanced gradients and deliberate colour relationships to create a polished glass-like finish. Combine glass with ceramic, stone, metal and matte materials where appropriate.

This must be engineered as a coherent design system, not applied as a cheap blur filter:
- Build reusable surface/material components with consistent tokens for tint, opacity, blur, border, highlight, shadow, elevation and interaction state.
- Create distinct resting, pressed, focused, selected, disabled and loading states.
- Use controlled accent colour and carefully balanced tonal contrast; avoid washed-out pale text on translucent surfaces.
- Preserve legibility over busy artwork with adaptive scrims, solid fallback surfaces or local contrast treatment.
- Keep blur and layered effects bounded on low-end devices; fall back gracefully to opaque or simplified surfaces when needed.
- Use meaningful colour, material and light to distinguish sections and controls without visual clutter.
- Make important controls feel physically intentional: clear hit area, alignment, press depth, tactile response and consistent geometry.

### No web-page design — absolute product-wide rule

**A web-page-like interface is not acceptable anywhere in Face It.** This applies to every screen, studio, editor, gallery, settings surface, onboarding flow, widget configurator, preview and tool—not only the home screen.

Do not design or implement the app as a responsive website squeezed into a phone:
- No generic website header/hero/three-column-card patterns.
- No desktop-style sidebars or navigation menus copied into narrow phone layouts.
- No long text-first landing pages, article-like feature descriptions or endless vertically stacked web cards.
- No generic web dashboard grids, marketing sections, oversized empty banners or browser-like page structure.
- No controls that resemble unstyled HTML forms or web admin panels.

Instead, design native Android experiences around the task: immersive artwork canvases, compact native-feeling toolbars, bottom navigation where appropriate, bottom sheets, contextual panels, gesture-aware editing, focused dialogs, segmented controls, native pickers and responsive Compose layouts. Use screen space intentionally and make the primary action obvious. On tablets or foldables, adapt the native composition intelligently; do not simply stretch a phone screen or turn it into a website.

All screens must be reviewed on real Android device dimensions. A screen that looks like a web template, generic card dashboard or plain text settings page fails visual acceptance even if it compiles.

### Avoid

- Generic blue/dark-blue AI dashboards.
- Excessive cards nested inside cards.
- Web-app-looking grids transplanted onto a phone.
- Large text explanations where a clear visual control would communicate better.
- Emoji used as primary product icons.
- Random floating brand marks or meaningless DS labels.
- Decorative glass effects that reduce contrast.
- Huge empty hero areas, cramped settings lists or inconsistent spacing.
- Tiny low-contrast labels, ambiguous icons and hidden primary actions.
- Fake preview content, example documents, dead buttons and placeholder workflows presented as real.
- Unnecessary onboarding copy or tutorial walls before the product is ready.

Use native-feeling Android navigation and interactions. Prefer purposeful full-bleed artwork, compact structured controls, clear toolbars, bottom sheets, segmented controls, preview canvases and contextual editors where appropriate. Each screen must have a clear primary action.

## 5. Product experience and navigation

The app should organise its major capabilities into a coherent, discoverable workspace. The current product areas—Discover, Create, Dialer, Library and Tools—may be refined, but preserve their conceptual separation unless a tested navigation improvement is demonstrably better.

- **Discover:** high-quality artwork and curated collections, visually led, fast to browse.
- **Create:** independent composition layers, editing tools, material and lighting controls, preview and save.
- **Dialer:** dialer keypad design and supported dialling experience.
- **Keyboard Designs:** a gallery of selectable keyboard visual styles with isolated, pressable in-app demos; not a system keyboard.
- **Library:** saved compositions, imported artwork, favourites and locally available content.
- **Tools:** widgets, keyboard, Vault, SMS utilities and other independent tools, each with honest capability descriptions.

Do not make the home screen a wall of feature cards. Use strong editorial hierarchy and let the artwork establish the experience. Prioritise frequently used actions. Avoid meaningless icons or controls that do not lead to real functionality.

### First launch

Create a polished first-run experience only when the product is genuinely ready. It should be short, visual and optional:
- Welcome to Face It.
- Briefly explain the personalisation concept through real product visuals.
- Ask only for choices needed to configure the experience.
- Offer clear **Next**, **Skip** and **Skip all** paths where appropriate.
- Do not request permissions in advance; request them when a user activates the relevant feature.
- Never force users through a long tutorial before they can explore.

Until release-ready content and workflows exist, do not add sample documents, fake workflows, demo buttons or pretend content merely to fill screens.

## 6. Composition and visual creation engines

The composition engine is the central product differentiator. Treat each layer as independent, editable and persistent.

### Layer model

A saved composition may include:
1. Background scene or artwork.
2. Main object or sculpture, including optional transparent PNG artwork.
3. Object material and finish.
4. Lighting, shadows and atmosphere.
5. Motion and depth treatment.
6. Widgets and their appearance.
7. Icon style where supported.
8. Optional dialer keypad visual profile.
9. Other supported interface personalisations.

Changing one layer must not unexpectedly reset unrelated layers. Support clear per-layer reset, whole-composition reset, undo/redo where practical, duplicate, rename, save, preview and apply.

### Composition Studio requirements

- Show a large, accurate preview of the actual result.
- Keep background and foreground/object assignment independent.
- Support import from Android's document picker for PNG, JPEG and WebP, with validation and bounded decoding.
- Offer natural crop, scale, placement, alignment and transparency handling.
- Provide material choices with convincing visual rendering: natural stone, ceramic, frosted glass, brushed metal, marble and other well-designed finishes.
- Make lighting direction, softness, intensity and colour meaningful controls rather than decorative sliders.
- Provide subtle, intentional motion settings and a static/reduced-motion option.
- Preserve user compositions locally across process death and app updates.
- Clearly distinguish an actual rendered preview from a concept illustration.
- Apply wallpaper through supported Android APIs with explicit Home, Lock or Both selection where supported.
- Handle device/API limitations and failure states gracefully.

Existing reference directions include a football/stadium environment paired with a sculptural lion in natural stone, and a calm architectural city environment with geometric sculptural elements and warm natural light. Treat these as quality references, not fixed themes. The user must be able to recombine categories freely.

### 3D and object studio

Build on-demand, interactive 3D experiences only where they add real value:
- Rotate objects with touch gestures.
- Inspect materials and lighting.
- Adjust surface finish, roughness, reflections and shadows.
- Support optional exploded/reassembly or component inspection where the model genuinely contains parts.
- Keep rendering paused when the studio is hidden.
- Provide a high-quality static fallback for devices that cannot sustain real-time 3D.

Do not simulate 3D with a static image while claiming interactive 3D. Do not make the entire app depend on a heavy 3D engine.

## 7. Wallpaper, artwork and internal asset library

The long-term curated collection target is **at least 100 distinct, high-quality portrait PNG wallpapers**. The 100-wallpaper collection is a release target, not an existing accomplishment. Do not invent counts or pass low-quality placeholders as finished artwork.

### Collection breadth

Build a rich, coherent library covering:
- World cities, landmark architecture and historic urban environments.
- Geometric art, ornamental craft, calligraphic forms and architectural detail.
- Football and wider sporting culture.
- Nature, wildlife, landscapes and atmospheric scenes.
- Automotive, transportation and industrial design.
- Professional fields such as medicine, science, engineering, law, finance and technology.
- Fashion, luxury materials, jewellery and refined product photography.
- Abstract, sculptural, 3D and experimental visual art.
- Seasonal, cultural and heritage collections integrated naturally into relevant architectural, city, geometric and art collections.

Culturally significant and sacred-site visual material must be handled respectfully and accurately. Integrate suitable architecture, cityscapes, geometric forms, ornament and heritage imagery naturally into the broader collection taxonomy. Do not create a sensationalised or careless treatment, invent inscriptions, or use meaningful text as random decoration.

### Asset quality bar

Every shipped asset must be:
- High resolution and correctly composed for modern phone aspect ratios.
- Free from visible compression artefacts, watermarks, accidental text and broken geometry.
- Properly cropped with safe areas considered for clock, icons and widgets.
- Colour graded consistently within its collection.
- Original or accompanied by verifiable usage rights.
- Correctly titled, categorised and described.
- Recorded in a manifest with stable ID, path, dimensions, creator/rights holder, licence, provenance and SHA-256.
- Reviewed at full-screen size on a real phone.

No scraping or unlicensed reuse of photographers, athletes, clubs, team marks, brands, artists or other creators. Generated assets must be reviewed for malformed architecture, distorted objects, gibberish text and other artefacts before inclusion.

### Asset engineering

- Keep bundled source assets under the established asset directories.
- Maintain the artwork manifest and run the validation tool.
- Generate thumbnails and size-appropriate derivatives through a reproducible pipeline.
- Decode only what is visible or requested.
- Use bounded caches and predictable memory use.
- Do not decode the entire library at once.
- Do not automatically download future collections.
- Measure APK size and startup impact before adding large packs.
- Keep user-imported artwork separate from curated bundled artwork.
- Never overwrite a user's original imported file during editing.

## 8. Widget Studio and phone control surfaces

Widgets and control surfaces must be real Android integrations, not decorative mockups.

### Native widget studio

Provide a cohesive studio to create and configure widgets, with accurate previews and actual installed widget behaviour. Start with the native clock/date widget and expand carefully to supported widget types such as:
- Clock and date.
- Calendar/agenda where data access is explicitly authorised.
- Photo/artwork frame.
- Shortcuts and quick actions.
- Other useful, permission-appropriate information widgets.

Widget controls should include layout, typography, material, colour, alignment, background opacity, corner shape, size and supported tap action. Provide per-widget configuration and independent saved settings.

Respect Android AppWidgetProvider lifecycle and launcher-specific sizing. Use appropriate update intervals; do not run a permanent animation loop or continuous 3D renderer for a widget. Handle widget resize, configuration, restore and removal. Explain that the launcher controls widget placement and may impose layout limitations.

### Control units and system surfaces

Explore every legitimate Android personalisation surface that can improve the user's experience: app widgets, shortcuts, supported quick settings tiles, notifications, lock-screen-related surfaces where the OS permits, wallpaper, launcher-compatible icons and app-owned controls.

For each integration:
- Identify the exact Android API, role, permission and OS-version support.
- Build a real implementation with a clear user consent path.
- Provide an accurate preview and graceful fallback.
- Do not claim control over manufacturer-only features or third-party app UI.
- Never promise universal control over the notification shade, volume keys, navigation bar, status bar, lock screen or another app's controls.

The app may offer a unified **Control Studio** to configure the integrations Face It can genuinely support. The studio must clearly distinguish app-owned controls from Android system integrations.

## 9. Dialer keypad studio — separate from text keyboard

The phone dialer number pad and the keyboard-design studio are two distinct visual experiences and must remain separate in navigation, code, settings and documentation. The keyboard-design studio is for selectable visual designs and in-app pressable demonstrations; it is not a system text-entry keyboard.

### Dialer keypad

The approved visual direction is a premium, raised, individually separated, tablet-like number pad. Digits should sit on sculpted key surfaces with carefully controlled highlights, contact shadows, depth and tactile press feedback.

Support material families such as:
- Sculpted ceramic.
- Frosted glass.
- Natural stone.
- Brushed metal.
- Matte and translucent finishes.

Provide carefully designed states for digits 0–9, star, hash, delete and call. Digits must remain instantly readable. Make key geometry, spacing, typography, contrast, surface finish and press depth configurable. Check touch targets and one-handed use.

The current `ACTION_DIAL` handoff is not a replacement for the phone manufacturer's dialler. If implementing a full default dialer, satisfy Android's dialer role, required components and permissions, user consent, emergency-call expectations and telephony testing. Never suggest that Face It can reskin an arbitrary third-party Phone app.

## 10. Keyboard appearance studio — selectable visual designs, not a replacement keyboard

**Product-owner clarification — binding decision:** The keyboard idea is a visual personalisation studio. It is NOT a request to build, install, activate or replace the user's system text-entry keyboard. Do not interpret the request as an InputMethodService, a new typing engine, a replacement keyboard app, or a phone dialer keypad.

The user's intent is to let people browse, preview, compare and select different **keyboard designs / visual styles** as personalisation concepts. These are selectable design treatments and visual presets. They may be shown in an interactive in-app demonstration so users can press keys and experience the design, but those demo keys are not connected to real system text input and must never intercept or transmit typed content.

### Required experience

- A dedicated Keyboard Designs gallery/studio with a high-quality visual preview for each design.
- Multiple genuinely distinct design families: refined glass, frosted glass, ceramic, stone, metallic, soft matte, translucent, sculpted and other carefully art-directed styles.
- Distinct keycap geometry, spacing, typography, legends, surface finish, highlights, shadows, depth, palette and press-state animation.
- Design variations for different visual tastes and screen proportions; avoid simply recolouring the same weak layout.
- Users can inspect a design, switch between styles, compare previews, favourite designs and select a preferred visual design within Face It.
- A clearly labelled **Try demo** / **Preview interaction** mode may allow users to press demonstration keys and see tactile/visual feedback. The demo is isolated inside Face It and does not type into other apps.
- Demo mode must be obviously a demonstration, not a functioning system keyboard. Provide a clear exit.
- Persist the selected design and preview preferences locally.
- Make clear which choices are only Face It visual presets and which, if any, Android integration is genuinely supported in a future separately authorised scope.

### Explicitly prohibited without a new product-owner decision

- Do not register or ship an InputMethodService as the intended implementation of this keyboard-design request.
- Do not ask users to enable or select Face It as their Android system keyboard.
- Do not claim selected visual presets change Gboard, Samsung Keyboard or any third-party keyboard.
- Do not claim a Face It design is active system-wide unless a real, supported platform capability has been implemented and separately approved.
- Do not merge the keyboard-design gallery with the phone dialer number pad. They are distinct visual studios.
- Do not build a fake system integration or use misleading system-settings instructions to make a demo look active.

The correct deliverable is a beautiful, pressable **in-app design preview**, plus a persistent selected design preference. A future system-keyboard integration would require a new explicit product-owner decision and a separate privacy, platform and implementation review.

## 11. Private Vault and sensitive data

Vault must be a real secure local storage feature. It must not rely on a decorative lock screen.

- Use Android Keystore-backed cryptographic keys and authenticated encryption such as AES-256-GCM.
- Store private content in app-private, no-backup storage unless the user explicitly opts into a separately designed backup.
- Authenticate before decrypting or exporting protected content.
- Never silently generate a replacement key when encrypted records still exist.
- Design recovery before promising account recovery or device migration.
- If biometric authentication is added, use AndroidX Biometric/BiometricPrompt correctly, with a deliberate fallback policy and tested key invalidation behaviour.
- Handle key loss, device lock changes, failed authentication, corrupt ciphertext, deletion and export explicitly.
- Avoid retaining plaintext buffers or temporary files longer than necessary.
- Do not claim biometric protection or recovery until implemented and verified.

Optional encrypted cloud backup is not part of the offline core. It requires explicit consent, end-to-end encryption, a verified recovery design and clear deletion semantics.

## 12. SMS composer and messaging boundaries

The SMS Composer may provide local typography, colour and layout previews, but ordinary SMS transport is plain text. Never imply that fonts, colours or rich formatting are transmitted over SMS.

- Preserve the original message body.
- Provide an explicit handoff to the user's chosen SMS app.
- Keep segment estimation accurate for GSM-7 and UCS-2, including multipart behaviour and extension characters.
- Label segment counts as estimates when Android/carrier behaviour can vary.
- Never send messages silently.
- Do not request broad SMS permissions if a safe handoff is sufficient.
- No advertising or unrelated prompts in sensitive messaging flows.

## 13. Adaptive Resource Engine — mandatory shared infrastructure

The Adaptive Resource Engine is a core runtime policy, not a decorative settings page.

### Profiles

- **Essential:** static artwork, minimal effects, no continuous 3D; suitable for low-memory or battery-constrained phones.
- **Balanced:** recommended default, polished transitions and on-demand effects.
- **Immersive:** optional higher-quality lighting, effects and interactive 3D, only within device limits.

### Device adaptation

Support a broad Android device range, including entry-level phones, different RAM capacities, GPU capabilities, refresh rates, aspect ratios, densities, thermal conditions and Android API levels.

- Inspect capability at runtime; do not assume flagship hardware.
- Adapt texture sizes, shadow quality, blur, frame rate and animation complexity.
- Respect Android thermal status and battery constraints.
- Pause animations when screens are hidden, backgrounded or not interacted with.
- Release bitmaps, textures, framebuffers and temporary resources promptly.
- Avoid unnecessary wake locks, polling, foreground services and background work.
- Use event-driven updates and WorkManager only for appropriate deferrable work.
- Bound memory, disk and thumbnail caches.
- Provide static fallbacks and reduced-motion support.
- Keep startup fast and avoid loading large models or libraries on launch.

Every feature team must use the shared resource policy rather than creating independent uncontrolled rendering loops.

## 14. Android compatibility and platform honesty

The current foundation is native Kotlin and Jetpack Compose. Continue the established native Android architecture unless a documented engineering decision demonstrates a better route. Premium engineering patterns from other frameworks may inform interaction and design, but do not migrate frameworks or rewrite working native capabilities without a measured technical case.

Target a wide range of Android phones:
- Respect minimum supported API and runtime API checks.
- Handle small, tall, wide and foldable layouts where practical.
- Respect display cut-outs, system bars, gesture navigation and edge-to-edge rules.
- Support font scaling, display scaling and different densities.
- Avoid hard-coded screen dimensions.
- Use adaptive layouts and responsive constraints.
- Test Android 8.0/API 26 baseline behaviour through current supported Android versions.
- Treat manufacturer customisations as compatibility variables, not guarantees.
- Use supported Android roles, intents, providers, document pickers and permissions.
- Do not bypass platform security or claim access to inaccessible system surfaces.

A feature that cannot be implemented universally must state its compatibility boundary and provide the best supported alternative.

## 15. Architecture and code quality

Keep the project feature-oriented, testable and maintainable. Separate:
- Presentation and navigation.
- Domain models and composition rules.
- Local persistence and asset metadata.
- Android platform adapters.
- Rendering and graphics resources.
- Shared design tokens and reusable components.
- Resource and lifecycle coordination.

Use clear models for compositions, artwork metadata, material settings, widget configurations and user preferences. Avoid giant Compose screens, duplicated state, hard-coded content scattered through UI, hidden global mutable state and business logic inside composables.

Use coroutines and lifecycle-aware collection correctly. Keep main-thread work short. Validate imports and external data. Handle cancellation and process recreation. Add unit tests for deterministic engines and UI/platform tests where valuable.

Do not add dependencies without a clear need, maintenance assessment and size/runtime cost review. Prefer native Android APIs and local deterministic algorithms over paid services or unnecessary cloud APIs.

## 16. Interaction, motion and haptics

Every interaction must have a clear response:
- Press, focus, selected, disabled, loading, success and error states.
- Meaningful touch feedback and optional haptics.
- Smooth, restrained transitions with no gratuitous motion.
- Undo or confirmation for destructive actions.
- Clear progress for long-running import, render or export tasks.
- Helpful empty states that guide users to a real next action.
- Recoverable errors with useful language.

Do not use a slider when a discrete choice is clearer. Do not use decorative toggles that have no persistent effect. Every preference must either change real behaviour or be removed.

## 17. Accessibility and inclusive use

Accessibility is part of product quality:
- Strong text and control contrast in light and dark modes.
- Dynamic type and system font scaling.
- Semantics and content descriptions for meaningful controls.
- Minimum comfortable touch targets.
- Screen-reader support and logical traversal.
- Reduced-motion support.
- No status conveyed by colour alone.
- Keyboard navigation and switch-access compatibility where relevant.
- Respect system accessibility settings.
- Avoid unnecessary visual effects that reduce clarity.

Review every important screen at enlarged font sizes and on smaller displays.

## 18. Security, permissions and privacy

- Offline-first core; no mandatory account.
- Request each permission only when the user activates its feature.
- Explain the purpose before invoking sensitive platform flows.
- Minimise data collection and network traffic.
- Do not transmit personal artwork, typed text, Vault content or message content without explicit, informed user action.
- Use app-private storage for private imports.
- Validate file type, size, dimensions and decoded allocation before processing.
- Keep third-party libraries and content rights documented.
- Provide clear export and deletion controls.
- No deceptive permission prompts or dark patterns.

## 19. Quality gates and definition of done

A screen is not a feature. A visual mock-up is not a working engine. A successful compile is not proof of a production-ready app.

A feature is complete only when all relevant items pass:

1. **Product:** user problem, workflow and platform boundary are clear.
2. **Visual:** polished final design, coherent tokens, correct assets and responsive states.
3. **Functional:** every control performs the advertised operation.
4. **Persistence:** state survives process death and app restart where expected.
5. **Offline:** core workflow functions without network access.
6. **Failure handling:** cancellation, permission denial, missing files, storage pressure and API limitations are handled.
7. **Accessibility:** contrast, touch targets, semantics, font scaling and reduced motion are reviewed.
8. **Performance:** startup, memory, CPU/GPU, battery and cache behaviour are measured.
9. **Security:** sensitive flows, permissions, storage and data boundaries are reviewed.
10. **Tests:** relevant unit/instrumentation tests pass.
11. **Device validation:** install and exercise on physical Android devices representing different capability levels before release claims.
12. **Documentation:** implementation status and known limitations are accurate.

Never mark a planned feature complete because its screen or settings exist.

### Required test discipline

Run the repository's actual validation and build commands. At minimum, use the project's Gradle unit-test and build tasks, artwork validation and any relevant lint/static checks. Record exact results. CI success must not be described as physical-device verification.

For visual review, inspect actual screenshots from the built application—not only Compose previews. Review light/dark, small/large display, font scaling and important interaction states.

## 20. Release discipline, signing and versioning

- Keep debug and release application identities clearly distinguished.
- Never publish a debug APK as a production release.
- Release signing must use a protected, durable keystore and secure secret handling.
- Never commit keystore files, passwords, signing properties or private keys.
- Back up the release key securely; loss can prevent future updates.
- Verify package ID, version code, version name, manifest, icon, permissions and APK signature.
- Test installation, upgrade and uninstall behaviour.
- Produce a release note with known limitations.
- Do not claim the 100-wallpaper pack or any unfinished engine is included.
- Keep the app size and install footprint reasonable; large optional content should not inflate the base APK unnecessarily.

## 21. Priority order for agents

Work in this order unless a blocking defect requires a different sequence:

**P0 — Identity and quality foundation**
- Professional Face It logo and integrated high-resolution PNG/adaptive icon resources.
- Consistent premium design system, typography, spacing, light/dark surfaces and meaningful navigation.
- Remove amateur, random, placeholder and non-functional UI.

**P1 — Core visual composition**
- Stable independent layer architecture.
- Excellent live composition preview and import workflow.
- High-quality material, lighting, crop, placement and apply controls.
- Persistence and reliable wallpaper application.

**P2 — Real phone personalisation engines**
- Native widget studio and widget configuration.
- Dialer keypad studio and supported dialling integration.
- Keyboard-design gallery, premium selectable visual presets and isolated pressable demonstrations.
- Launcher-compatible icon personalisation and honest platform guidance.
- Supported control surfaces and shortcuts.

**P3 — Private and communication tools**
- Vault authentication, key-loss/recovery design and secure lifecycle.
- SMS composer accuracy and safe handoff.

**P4 — Content and advanced experiences**
- Curated, provenance-verified 100+ PNG wallpaper collection.
- Object and material asset library.
- On-demand 3D studio and carefully controlled advanced effects.
- Additional curated visual universes and optional content packs.

This order does not mean content quality is unimportant. It means the app's identity and usable engines must be excellent before a large wallpaper count is used to disguise weak product foundations.

## 22. Agent execution rules

Every agent must:

- Treat this file as an active instruction, not optional reading.
- Work on the existing repository and current primary branch unless explicitly instructed otherwise.
- Inspect before editing; preserve useful implementation and avoid unnecessary rewrites.
- Prefer complete, working vertical slices over broad collections of empty screens.
- Build reusable components and shared design tokens.
- Make the smallest coherent change that improves quality.
- Add tests for engine logic and regression-prone behaviour.
- Keep asset provenance and licence records.
- Report what changed, files touched, commands run, test outcomes and remaining limitations.
- Distinguish verified facts from assumptions.
- Never fabricate screenshots, test results, assets, permissions, capabilities or release readiness.
- Never add sample workflows, fake documents or nonfunctional controls to make a screen look populated.
- Never remove an existing feature or user data without a clear migration and justification.
- Update this directive only when the product owner changes a decision; explain the change in the commit.

### Completion report format

At the end of each substantial task, report:

1. **Delivered:** actual implemented behaviour and visual changes.
2. **Files:** principal files changed or added.
3. **Verification:** exact build, test, validation and device checks performed.
4. **Not yet done:** honest remaining work and platform limitations.
5. **Next priority:** the most valuable next engineering step.

---

## Final product standard

Face It must earn trust at first glance and retain it through daily use. The icon, interface, artwork, widgets, keypad surfaces and every control must feel like parts of one carefully designed product. Beautiful appearance and real engineering are equally mandatory. Never ship a visual promise in place of a working capability.


## 21. Unified personalisation studios — expanded product-owner direction

Face It must bring themes, native widgets, icon design, app presentation, keyboard appearances, controls, charging visuals and display treatments into one coherent Style Studio. Users should be able to explore related design families, preview the result on a device canvas and save local style choices. Do not turn this into a generic web dashboard or a list of disconnected feature cards.

### Required visual collections

- Build a dedicated optional feminine collection with professionally art-directed colour palettes, cats and other cute animals, floral and jewellery-inspired details, colourful app-symbol families, pearl, satin, ceramic and soft-glass materials. It must remain one creative universe among many; never force gendered assumptions or lock users out of other styles.
- Include an optional **Niqab-inspired display frame**: a respectful, refined draped visual frame around a device/app-grid preview. Users can independently choose its colour (Onyx, Rose, Plum, Pearl, Sage, Sand), combine it with other wallpapers and visual treatments, and turn it off. Do not reduce it to a costume icon or use it as a mandatory app mask.
- Keep the visual preview beautiful and recognisable as a phone composition: real app-symbol artwork, widgets, display content and contextual status elements should appear within the frame opening. Avoid a generic card dashboard.

### Feature truth and Android integration boundaries

- Themes and palettes saved in Style Studio are Face It presets unless a supported Android setting integration is actually implemented.
- Widgets must be real AppWidget providers. The existing clock widget supports Obsidian Glass, Warm Ceramic and Pearl Mint. Preview selection does not silently alter existing widget instances; Android owns placement and resize.
- Icon families and app-grid designs are previews unless a supported launcher/icon-pack or pinned-shortcut integration is implemented. Never promise universal third-party launcher icon replacement.
- Keyboard designs remain in-app visual presets and isolated pressable demos only. Do not add an InputMethodService.
- Quick Settings/control panel concepts must not claim to replace or reskin Android's system controls.
- Charging animations may be previewed in-app. Do not claim a system charging overlay without a separately approved, permission-correct Android implementation; respect background-launch and manufacturer restrictions.
- Display treatments are in-app previews unless an explicit supported platform action (such as wallpaper application) is invoked.

Each category must have clear capability labels, local persistence where appropriate, a reduced-motion/static fallback, accessible contrast and a coherent route to its actual supported implementation. Never represent a preview as a completed OS-level integration.
