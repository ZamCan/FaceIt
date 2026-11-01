# Adaptive Resource Engine
The Adaptive Resource Engine is shared runtime policy, not a decorative battery-saver screen.
## Profiles
Essential: static assets, no continuous 3D, minimal effects. Balanced: default, standard transitions and on-demand effects. Immersive: opt-in advanced rendering within device limits.
## Lifecycle
No renderer starts before its feature is opened. Stop animation when hidden or backgrounded. Release textures, framebuffers and temporary bitmaps when finished. Avoid persistent rendering, polling and unnecessary wake locks. Widgets use platform-appropriate updates.
## Adaptive quality
Use device capability, memory pressure, battery and Android thermal status to adapt texture resolution, shadows, frame rate and effects.
## Artwork
Lazy-load, bound caches, separate thumbnails/previews/full-resolution assets and remove expendable preview cache under storage pressure. Do not automatically download future packs.
## Acceptance
No renderer while no 3D feature is visible; no hidden animation loop; no retained temporary GPU resources; bounded cache; Essential mode works on low-memory devices.
