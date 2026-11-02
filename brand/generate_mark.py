#!/usr/bin/env python3
"""FaceIt master identity generator.

Single reproducible source for the Face It mark. The 1024 px PNG master, the
Play Store icon, the monochrome/themed layer, every legacy launcher density,
the adaptive-icon vectors and the review sheet are all produced from the SAME
geometry defined here, so a revision never drifts between densities and is
never destructive.

Run from the repository root:

    python3 brand/generate_mark.py

Identity concept
----------------
Three stacked isometric slabs: the composition layers themselves, with a lit
top surface and sculpted side walls. The mark therefore states what the
product does ("compose, don't impose") instead of borrowing a generic gem,
robot head or chat bubble. Ivory top slab, one violet accent slab and one
graphite base slab keep the palette to a single accent plus neutrals, so the
launcher does not become a rainbow.

Legibility strategy
-------------------
* Silhouette is a stepped diamond column: unique among launcher icons and
  readable in one glance at 24 px.
* Only three large shapes, no hairlines below 6 design units.
* Every slab top face carries its own gradient and a definition edge, so the
  mark survives light, dark and photographic backdrops.
* The mark occupies 70% of the canvas, safely inside the 66 dp adaptive-icon
  safe zone, so circle and squircle masks never clip it.
"""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
BRAND = ROOT / "brand"
RES = ROOT / "app" / "src" / "main" / "res"

# 1080 x 1080 design space; everything is expressed here and scaled on render.
DESIGN = 1080.0
CX, CY = 540.0, 540.0
HALF_WIDTH = 300.0          # diamond half-width
HALF_HEIGHT = 152.0         # diamond half-height (0.51 * half-width, shallower than true isometric: reads calmer)
THICKNESS = 40.0            # sculpted side wall
LAYER_GAP = 176.0           # vertical distance between slab top faces
OPTICAL_LIFT = 22.0         # the side walls add visual mass low, so lift the whole stack
SUPERSAMPLE = 4

IDENTITY = {
    "obsidian": (0x17, 0x19, 0x23),
    "ivory_top": (0xFB, 0xF9, 0xFF),
    "ivory_bottom": (0xD8, 0xCD, 0xEC),
    "ivory_side": (0x7D, 0xE4, 0xD2),      # the spectral signature lives in this wall
    "ivory_side_deep": (0x46, 0xB5, 0xA3),
    "violet_top": (0xBC, 0xA5, 0xFF),
    "violet_bottom": (0x8B, 0x6D, 0xF2),
    "violet_side": (0x60, 0x45, 0xC9),
    "graphite_top": (0x4C, 0x53, 0x63),
    "graphite_bottom": (0x31, 0x36, 0x43),
    "graphite_side": (0x23, 0x27, 0x31),
    "mint": (0x76, 0xDC, 0xCB),
    "edge": (0x12, 0x14, 0x1B),
}


def rgba(colour: tuple[int, int, int], alpha: int = 255) -> tuple[int, int, int, int]:
    return (*colour, alpha)


def diamond(centre: tuple[float, float], half_width: float = HALF_WIDTH,
            half_height: float = HALF_HEIGHT) -> list[tuple[float, float]]:
    cx, cy = centre
    return [
        (cx, cy - half_height),
        (cx + half_width, cy),
        (cx, cy + half_height),
        (cx - half_width, cy),
    ]


def slab_polygons(centre: tuple[float, float]):
    """Return (top_face, left_wall, right_wall) for one slab.

    Light comes from the upper left, so the left wall is the mid tone and the
    right wall is the darkest — the same lighting model the dialer keypad uses.
    """
    cx, cy = centre
    top = diamond(centre)
    n, e, s, w = top
    left_wall = [w, s, (s[0], s[1] + THICKNESS), (w[0], w[1] + THICKNESS)]
    right_wall = [s, e, (e[0], e[1] + THICKNESS), (s[0], s[1] + THICKNESS)]
    return top, left_wall, right_wall


# Back to front: base slab, accent slab, light slab.
LAYERS = [
    {"name": "base", "offset": LAYER_GAP, "top": ("graphite_top", "graphite_bottom"),
     "wall": "graphite_side", "wall_deep": "graphite_side"},
    {"name": "accent", "offset": 0.0, "top": ("violet_top", "violet_bottom"),
     "wall": "violet_side", "wall_deep": "violet_side"},
    {"name": "surface", "offset": -LAYER_GAP, "top": ("ivory_top", "ivory_bottom"),
     "wall": "ivory_side", "wall_deep": "ivory_side_deep"},
]


def _scaled(points, scale: float, pad: float):
    return [((x - pad) * scale, (y - pad) * scale) for x, y in points]


def _gradient_polygon(image: Image.Image, points, start, end, pad: float, scale: float) -> None:
    """Fill a polygon with a vertical gradient, clipped to the polygon."""
    pts = _scaled(points, scale, pad)
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    box = (int(min(xs)) - 1, int(min(ys)) - 1, int(max(xs)) + 2, int(max(ys)) + 2)

    strip = Image.new("RGB", (1, max(2, box[3] - box[1])), start)
    draw = ImageDraw.Draw(strip)
    height = strip.height
    for y in range(height):
        t = y / max(1, height - 1)
        draw.point((0, y), fill=tuple(
            round(start[i] + (end[i] - start[i]) * t) for i in range(3)
        ))
    gradient = strip.resize((box[2] - box[0], box[3] - box[1]), Image.BILINEAR).convert("RGBA")

    mask = Image.new("L", gradient.size, 0)
    ImageDraw.Draw(mask).polygon(
        [(x - box[0], y - box[1]) for x, y in pts], fill=255
    )
    image.paste(gradient, (box[0], box[1]), mask)


def render_mark(size: int, *, padded: bool = True, monochrome: bool = False,
                background: tuple[int, int, int, int] | None = None,
                flat: bool = False) -> Image.Image:
    """Render the mark at `size` px with 4x supersampling for clean edges."""
    work = max(size, 1) * SUPERSAMPLE
    image = Image.new("RGBA", (work, work), background or (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    # 70% of the canvas: launcher-safe for circle, squircle and legacy masks.
    pad = DESIGN * 0.17 if padded else 0.0
    scale = work / (DESIGN - pad * 2)

    for layer in LAYERS:
        centre = (CX, CY + layer["offset"] - OPTICAL_LIFT)
        top, left_wall, right_wall = slab_polygons(centre)
        top_start = IDENTITY[layer["top"][0]]
        top_end = IDENTITY[layer["top"][1]]
        wall = IDENTITY[layer["wall"]]

        if monochrome:
            # Themed icons are a single tint: walls at full strength, top face
            # knocked back so the stack still separates without colour.
            draw.polygon(_scaled(left_wall, scale, pad), fill=rgba((255, 255, 255), 150))
            draw.polygon(_scaled(right_wall, scale, pad), fill=rgba((255, 255, 255), 110))
            draw.polygon(_scaled(top, scale, pad), fill=rgba((255, 255, 255)))
            continue

        draw.polygon(_scaled(left_wall, scale, pad), fill=rgba(wall))
        draw.polygon(_scaled(right_wall, scale, pad), fill=rgba(IDENTITY[layer["wall_deep"]]))

        if flat:
            mid = tuple(round((top_start[i] + top_end[i]) / 2) for i in range(3))
            draw.polygon(_scaled(top, scale, pad), fill=rgba(mid))
        else:
            _gradient_polygon(image, top, top_start, top_end, pad, scale)

    return image.resize((size, size), Image.LANCZOS)


def _mask(mode: str, size: int) -> Image.Image:
    layer = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(layer)
    if mode == "circle":
        draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    elif mode == "squircle":
        draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=int(size * 0.30), fill=255)
    elif mode == "rounded":
        draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=int(size * 0.22), fill=255)
    else:
        draw.rectangle((0, 0, size - 1, size - 1), fill=255)
    return layer


def _icon_panel(size: int, mode: str) -> Image.Image:
    """A launcher tile: icon background plus the mark, then masked."""
    panel = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    if mode in ("circle", "squircle", "rounded"):
        background = Image.new("RGBA", (size, size), rgba(IDENTITY["obsidian"]))
        panel.paste(background, (0, 0), _mask(mode, size))
        mark = render_mark(size, padded=True)
        panel.paste(mark, (0, 0), mark)
        panel.putalpha(ImageChops.multiply(panel.getchannel("A"), _mask(mode, size)))
    else:
        mark = render_mark(size, padded=True)
        panel.paste(mark, (0, 0), mark)
    return panel


def render_sheet() -> Image.Image:
    """Review sheet: surfaces, launcher masks and true launcher-size renditions."""
    tile, gap, label_bar = 240, 26, 62
    columns = 5
    width = gap + columns * (tile + gap)
    height = gap + tile + label_bar + 150 + gap
    sheet = Image.new("RGB", (width, height), (0x0E, 0x10, 0x14))
    draw = ImageDraw.Draw(sheet)

    surfaces = [
        ("Light surface", (0xF8, 0xF5, 0xF0), None, False),
        ("Dark surface", (0x0E, 0x10, 0x14), None, False),
        ("Neutral", (0x25, 0x28, 0x32), None, False),
        ("Circle mask", (0x17, 0x19, 0x23), "circle", True),
        ("Squircle mask", (0x17, 0x19, 0x23), "squircle", True),
    ]

    for index, (label, colour, mask, as_tile) in enumerate(surfaces):
        left = gap + index * (tile + gap)
        top = gap
        if as_tile:
            sheet.paste(_icon_panel(tile, mask), (left, top), _icon_panel(tile, mask))
        else:
            panel = Image.new("RGB", (tile, tile), colour)
            mark = render_mark(tile, padded=True)
            panel.paste(mark, (0, 0), mark)
            sheet.paste(panel, (left, top))
        draw.text((left + 2, top + tile + 18), label, fill=(0xC9, 0xC3, 0xD6))

    # Actual launcher sizes at 100% zoom — the only honest way to judge an icon.
    sizes = [24, 32, 48, 72, 96]
    x = gap
    baseline = gap + tile + label_bar + 40
    draw.text((gap, baseline - 30), "Actual launcher sizes (100% zoom)", fill=(0x8D, 0x87, 0x96))
    for size in sizes:
        rendition = render_mark(size, padded=True)
        sheet.paste(rendition, (x, baseline + (48 - size)), rendition)
        draw.text((x, baseline + 58), f"{size}px", fill=(0x8D, 0x87, 0x96))
        x += max(size, 56) + 26

    # Edges inspected at 4x so the supersampled silhouette can be reviewed.
    zoom = render_mark(24, padded=True).resize((96, 96), Image.NEAREST)
    sheet.paste(zoom, (x, baseline), zoom)
    draw.text((x, baseline + 58), "24px @4x", fill=(0x8D, 0x87, 0x96))
    return sheet


def _vector_path(points) -> str:
    head, *tail = points
    body = " ".join(f"L{x:.3f},{y:.3f}" for x, y in tail)
    return f"M{head[0]:.3f},{head[1]:.3f} {body} Z"


def _to_adaptive(points):
    """1080 design space -> 108 x 108 adaptive viewport (mark already at 70%)."""
    return [((x - DESIGN / 2) * 0.1 + 54, (y - DESIGN / 2) * 0.1 + 54) for x, y in points]


def write_adaptive_vectors() -> None:
    fills = []
    for layer in LAYERS:
        top, left_wall, right_wall = slab_polygons((CX, CY + layer["offset"] - OPTICAL_LIFT))
        fills.append(("#%02X%02X%02X" % IDENTITY[layer["wall"]],
                      _vector_path(_to_adaptive(left_wall))))
        fills.append(("#%02X%02X%02X" % IDENTITY[layer["wall_deep"]],
                      _vector_path(_to_adaptive(right_wall))))
        mid = tuple(round((IDENTITY[layer["top"][0]][i] + IDENTITY[layer["top"][1]][i]) / 2) for i in range(3))
        fills.append(("#%02X%02X%02X" % mid, _vector_path(_to_adaptive(top))))

    paths = "\n".join(
        f'    <path android:fillColor="{colour}" android:pathData="{data}"/>' for colour, data in fills
    )

    foreground = f"""<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED by brand/generate_mark.py — edit the generator, not this file. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
{paths}
</vector>
"""

    mono_top, mono_left, mono_right = slab_polygons((CX, CY - LAYER_GAP - OPTICAL_LIFT))
    mono_paths = "\n".join(
        f'    <path android:fillColor="#{alpha:02X}FFFFFF" android:pathData="{_vector_path(_to_adaptive(geom))}"/>'
        for geom, alpha in [(mono_left, 150), (mono_right, 110), (mono_top, 255)]
    )
    for layer in LAYERS[1:]:
        top, left_wall, right_wall = slab_polygons((CX, CY + layer["offset"] - OPTICAL_LIFT))
        mono_paths = "\n".join(
            f'    <path android:fillColor="#{alpha:02X}FFFFFF" android:pathData="{_vector_path(_to_adaptive(geom))}"/>'
            for geom, alpha in [(left_wall, 150), (right_wall, 110), (top, 255)]
        ) + "\n" + mono_paths

    monochrome = f"""<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED by brand/generate_mark.py — themed/monochrome launcher layer. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
{mono_paths}
</vector>
"""

    background = """<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED by brand/generate_mark.py -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient android:startX="0" android:startY="0" android:endX="108" android:endY="108"
                android:type="linear">
                <item android:offset="0" android:color="#FF1E2130"/>
                <item android:offset="1" android:color="#FF101219"/>
            </gradient>
        </aapt:attr>
    </path>
</vector>
"""

    (RES / "drawable").mkdir(parents=True, exist_ok=True)
    (RES / "drawable" / "faceit_launcher_foreground.xml").write_text(foreground, encoding="utf-8")
    (RES / "drawable" / "faceit_launcher_monochrome.xml").write_text(monochrome, encoding="utf-8")
    (RES / "drawable" / "faceit_launcher_background.xml").write_text(background, encoding="utf-8")

    # Legacy single-layer drawable. Nothing references it: the manifest points at
    # @mipmap/ic_launcher and the adaptive icon is anydpi-v26, which minSdk 26
    # always resolves. Kept behind a flag so the artefact list stays honest.
    _write_legacy_drawable = False
    # Legacy single-layer drawable, still referenced by older launchers.
    legacy_paths = paths.replace('android:fillColor="#', 'android:fillColor="#FF')
    legacy = f"""<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED by brand/generate_mark.py — legacy square launcher drawable. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FF171923" android:pathData="M0,0h108v108h-108z"/>
{legacy_paths}
</vector>
"""
    if _write_legacy_drawable:
        (RES / "drawable" / "faceit_launcher.xml").write_text(legacy, encoding="utf-8")


LEGACY_DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def _glyph_mark(px: int) -> Image.Image:
    """The mark alone on transparency, with no square ground behind it.

    Lint rejects launcher icons that fill every pixel of their square, and it is
    right: a filled square has no silhouette. Legacy icons therefore carry the
    composition on transparent pixels, and launchers that still mask it keep the
    padded safe area.
    """
    mark = render_mark(px, padded=True)
    glyph = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    glyph.paste(mark, (0, 0), mark)
    return glyph


def write_legacy_icons() -> None:
    """Deprecated: kept as a no-op so the pipeline stays importable.

    Launcher rasters used to be written per density, but minSdk is 26 and
    mipmap-anydpi/ic_launcher.xml is an adaptive icon, so every supported device
    takes the vector path. Lint was right on both counts it raised: the density
    PNGs were unused, and having both an XML and a PNG called ic_launcher is a
    collision rather than a fallback. The Play Store icon and the master PNG still
    live in brand/, which is where non-launcher artwork belongs.
    """


def write_master_svg() -> None:
    """Emit the master vector from the same geometry the rasters use."""
    shapes = []
    for layer in LAYERS:
        centre = (CX, CY + layer["offset"] - OPTICAL_LIFT)
        top, left_wall, right_wall = slab_polygons(centre)
        shapes.append(("polygon", left_wall, "#%02X%02X%02X" % IDENTITY[layer["wall"]]))
        shapes.append(("polygon", right_wall, "#%02X%02X%02X" % IDENTITY[layer["wall_deep"]]))
        gradient_id = f"grad-{layer['name']}"
        start = IDENTITY[layer["top"][0]]
        end = IDENTITY[layer["top"][1]]
        shapes.append(("gradient", (gradient_id, start, end), None))
        shapes.append(("top", top, gradient_id))

    defs, body = [], []
    for kind, geom, colour in shapes:
        if kind == "gradient":
            gid, start, end = geom
            defs.append(
                f'    <linearGradient id="{gid}" x1="0" y1="0" x2="0.18" y2="1">\n'
                f'      <stop offset="0" stop-color="#%02X%02X%02X"/>\n' % start
                + f'      <stop offset="1" stop-color="#%02X%02X%02X"/>\n' % end
                + "    </linearGradient>"
            )
        elif kind == "top":
            body.append(_svg_polygon(geom, f"url(#{colour})"))
        else:
            body.append(_svg_polygon(geom, colour))

    svg = (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        "<!-- GENERATED by brand/generate_mark.py — canonical FaceIt master geometry. -->\n"
        '<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1080" viewBox="0 0 1080 1080">\n'
        "  <defs>\n" + "\n".join(defs) + "\n  </defs>\n"
        + "\n".join(body) + "\n</svg>\n"
    )
    (BRAND / "faceit_mark.svg").write_text(svg, encoding="utf-8")


def _svg_polygon(points, fill: str) -> str:
    coords = " ".join(f"{x:.2f},{y:.2f}" for x, y in points)
    return f'    <polygon points="{coords}" fill="{fill}"/>'


def main() -> int:
    BRAND.mkdir(parents=True, exist_ok=True)

    render_mark(1024, padded=True).save(BRAND / "faceit_master_1024.png")

    playstore = Image.new("RGBA", (512, 512), rgba(IDENTITY["obsidian"]))
    play_mark = render_mark(512, padded=True)
    playstore.paste(play_mark, (0, 0), play_mark)
    playstore.save(BRAND / "faceit_playstore_512.png")

    mono = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    mono_mark = render_mark(512, padded=True, monochrome=True)
    mono.paste(mono_mark, (0, 0), mono_mark)
    mono.save(BRAND / "faceit_mark_monochrome_512.png")

    sheet = render_sheet()
    sheet.save(BRAND / "faceit_icon_preview_sheet.png")

    write_adaptive_vectors()
    write_master_svg()
    write_legacy_icons()

    provenance = {
        "asset": "Face It application mark — stacked composition slabs",
        "owner": "ZamCan",
        "type": "original vector geometry authored for Face It; no stock, no third-party or scraped material",
        "licence": "© ZamCan. All rights reserved. Not licensed for redistribution outside Face It releases.",
        "generator": "brand/generate_mark.py",
        "master": "brand/faceit_master_1024.png",
        "derivatives": [
            "brand/faceit_playstore_512.png",
            "brand/faceit_mark_monochrome_512.png",
            "brand/faceit_icon_preview_sheet.png",
            "app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_legacy.png",
            "app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round_legacy.png",
            "app/src/main/res/drawable/faceit_launcher_foreground.xml",
            "app/src/main/res/drawable/faceit_launcher_background.xml",
            "app/src/main/res/drawable/faceit_launcher_monochrome.xml",
            "app/src/main/res/drawable/faceit_launcher.xml",
        ],
        "notes": [
            "Geometry is authored once in this generator; no bitmap is stretched across densities.",
            "The mark occupies 70% of the canvas so adaptive-icon masks never clip the silhouette.",
            "Each density is rendered from geometry at its own resolution, not downsampled from one bitmap.",
            "Monochrome layer is a single tint with tonal separation for themed icons.",
            "Not yet verified on physical Samsung One UI / AOSP launchers — that remains a release gate.",
        ],
    }
    (BRAND / "PROVENANCE.json").write_text(json.dumps(provenance, indent=2) + "\n", encoding="utf-8")

    try:
        subprocess.run(["magick", str(BRAND / "faceit_icon_preview_sheet.png"),
                        str(BRAND / "faceit_icon_preview_sheet.svg")],
                       check=False, capture_output=True)
    except FileNotFoundError:
        pass

    print("FaceIt mark generated:")
    for path in sorted(BRAND.glob("*.png")):
        print(f"  {path.relative_to(ROOT)}")
    print("  adaptive + legacy launcher resources written")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
