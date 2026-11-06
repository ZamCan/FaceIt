# The name engine

A wallpaper that says **ZAMCAN** in warm metal is not a picture of a name; it is a
treatment. The product owner asked for that treatment with the name left editable
— "let that one written Naomi have another png template if alike but editable at
name section" — so this document records what was built, why it is built this way,
and what it deliberately does not do.

## What it is

Type a name. Choose a finish, a script and a setting. Choose where it sits and
what photograph it sits on. The name appears on the wallpaper, on the workspace
preview and on Face It Home, identically, because all three compose through
`graphics/WallpaperComposition.kt`.

| Layer | What the user controls |
|---|---|
| Words | Anything they type, up to 28 characters, in any script the device can render |
| Finish | 15 metals: rose gold, gold, champagne, platinum, obsidian, blush, coral, tangerine, lilac, mint, turquoise, sapphire, emerald, ruby, ivory |
| Script | Script (cursive serif), Serif, Sans, Mono |
| Setting | Normal, Wide, Tight — tracking per script, not one number for all four |
| Height | A slider from 12% to 88% of the screen |
| Plate | An engraved plate behind the type, optional |
| Ground | Any of the 161 bundled wallpapers, or the finish's own etched backdrop |

## Why the letterforms are the device's fonts

Two rounds of hand-drawn cursive letterforms were authored and rejected during
development. They looked acceptable for "ZamCan" and "Naomi" and then produced
"Naorni" for "Naomi" and "ZarnCan" for "ZamCan" once the words changed — a fixed
alphabet of hand-authored strokes is a font, and a font written in an afternoon is
a bad font.

The decision, and the reason it is not a shortcut:

1. **A name is arbitrary personal data.** It can be any script, any length, any
   diacritic. Names this app will be handed in Tanzania and the region include
   `Ng'ang'a`, `Qureshi`, `Šimon`, `Mwakyusa`, `佐藤`, `عائشة`. A hand-drawn Latin
   alphabet mangles everything outside it; a font falls back and still sets them
   correctly.
2. **Real glyphs stay real text.** They can be selected, read aloud by a screen
   reader and copied. This is the same rule as `text/WritingStyles.kt`, which
   refuses Unicode look-alike characters for exactly this reason.
3. **Nothing is bundled.** Only `FontFamily.Serif`, `SansSerif` and `Monospace`
   are used, so no font file ships without a licence and no name can fail to load.

What is engineered here is the light, not the letterform: the metal gradient, the
specular band, the bloom, the etched plate. That is what makes a name look
expensive, and it works for any string.

## How the metal works

`NameMonogram.draw` makes three passes over the same glyph run:

1. **Bloom** — two blurred copies of the type in the finish's own glow colour, at
   two radii. Blurring the glyphs rather than the frame keeps the halo tied to the
   letters and the cost flat regardless of screen size.
2. **Metal** — a vertical ramp of shadow → base → highlight → base → shadow, which
   is what reads as a polished surface rather than as coloured paint.
3. **Specular** — one narrow highlight band across the cap height, clipped to the
   glyphs, so the letters look struck rather than filled.

Type is measured once at a reference size and then scaled to 86% of the width, so
a name fits at any resolution. Long names are set smaller; they are never clipped,
never wrapped mid-word and never truncated with an ellipsis.

## What it does not claim

* **It does not recolour Android or other apps.** It draws Face It's own surfaces.
* **It does not upload the name.** Composition happens in `Dispatchers.IO` on the
  device; there is no network call anywhere in this path, which is the same offline
  guarantee the rest of the app keeps.
* **It does not turn the name into an image-only artefact at input time.** The name
  is stored as text in `PersonalEnvironment` and rasterised at render time, so it
  stays editable forever.
* **Generated grounds are labelled.** A photograph that was generated rather than
  photographed says so on its own card, on the same rule the whole pack follows.

## Where it lives

| File | Role |
|---|---|
| `graphics/NameMonogram.kt` | Finishes, scripts, settings, `NameDesign`, the renderer, the etched circuit ground |
| `graphics/WallpaperComposition.kt` | The single composition rule shared by workspace, studio, launcher and pinning |
| `ui/NameStudio.kt` | The studio surface, its preview and its shelves |
| `data/PersonalEnvironment.kt` | The name fields and `nameDesignOrNull()` |
| `app/src/test/.../NameMonogramTest.kt` | 15 tests over the text contract |
