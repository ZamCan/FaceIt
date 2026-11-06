# FaceIt — Design System
## Brand
FaceIt — Personalise Your World. Geometric crystalline identity; premium, global, creative and culturally respectful.
## Visual principles
Light and dark are first-class. Iridescence is a restrained accent. Materials include frosted glass, natural stone, ceramic, brushed metal and matte surfaces. Artwork is the hero; controls remain legible. Avoid generic card dashboards and meaningless brand marks.
## Tokens
Obsidian #111318; Graphite #252832; Cloud #E9E8EF; White #FFFFFF; Violet #A78BFA; Mint #76DCCB; Peach #F0C4A2; Ice #91B9F4.
## Geometry
4dp spacing base; 48dp minimum touch target; 20–24dp standard icons. Dialer keys are individually separated, raised tablet-like forms with readable digits, contact shadows and clear press states. Text-entry keyboard and phone dialer keypad are separate features.
## Accessibility
Contrast, scalable text, screen-reader labels, reduced motion, keyboard navigation and non-colour status cues are release requirements.

## Shared glass finish
Cards, preview containers and the navigation dock use the same optical treatment
in `ui/GlassSystem.kt`: a substantial neutral substrate, diagonal tint, soft
contact shadow and a restrained upper-edge reflection. Accent surfaces keep the
neutral substrate rather than making body text sit directly on saturated artwork.
No full-screen blur, perpetual shimmer or new runtime dependency is required.

Clickable glass surfaces use Material ripple/focus handling and a short 1.5%
press compression. Reduced-motion mode skips compression and contact shadows.
The bottom navigation is inset, rounded and system-inset-aware. Small labels
use 11.5sp type; back and segmented controls reserve at least 48dp.
Segmented choices expose a radio group and selected state to accessibility tools.

Discover uses a bounded artwork canvas and scrollable controls on smaller
displays. The workspace has one navigation scaffold; opening the gallery or a
studio never resets the destination during composition.

These are implemented design changes, not a physical-device visual acceptance
record. Review built screenshots in light/dark, enlarged type and compact displays
before release.
