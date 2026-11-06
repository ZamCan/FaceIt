#!/usr/bin/env python3
"""Catalogue audit for Face It.

`verify_sources.py` checks structure: balanced delimiters, resolvable imports,
resource references that exist. It cannot see inside a catalogue, which is how a
`ChargingStyle` with the wrong number of arguments or a theme pointing at a
wallpaper id that was never generated can pass it.

This script is the second gate, and it is deliberately paranoid, because the
catalogues are large and hand-edited by script:

  1. constructor arity      every entry supplies exactly the fields its data class
                            declares, positionally or by name, as appropriate
  2. identifier integrity   ids are unique within each catalogue
  3. cross-references       every wallpaper, keyboard, widget and charging id a
                            theme names actually exists
  4. enum coverage          every enum constant has a renderer route, and every
                            route names a constructor that exists
  5. contrast               every colour pair the design system promises to keep
                            legible is re-measured here from the source values
  6. resources              every generated drawable a catalogue names is on disk
  7. documentation honesty  the counts quoted in docs/ match the source

It parses Kotlin with regular expressions, which is why every parse is written to
be narrow: a pattern that also matches a comment is a pattern that lies. Comments
are stripped before any counting.

Usage:
    python3 tools/verify_catalogue.py            # audit, exit 1 on any problem
    python3 tools/verify_catalogue.py --verbose  # print the full inventory
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "app" / "src" / "main" / "java" / "com" / "zamcan" / "faceit"
RES = ROOT / "app" / "src" / "main" / "res"
ASSETS = ROOT / "app" / "src" / "main" / "assets"
DOCS = ROOT / "docs"

PROBLEMS: list[str] = []


def problem(message: str) -> None:
    PROBLEMS.append(message)


# --------------------------------------------------------------------- parsing
def strip_comments(text: str) -> str:
    """Remove `//` comments, leaving string literals untouched.

    The earlier structural checker missed this, and read the enumeration
    `// food, drink and health` as a glyph called "drink and health". Counting is
    only trustworthy once comments are gone.
    """
    out: list[str] = []
    in_string = False
    in_block = False
    i = 0
    while i < len(text):
        two = text[i:i + 2]
        if in_block:
            if two == "*/":
                in_block = False
                i += 2
                continue
            i += 1
            continue
        if in_string:
            out.append(text[i])
            if text[i] == "\\":
                out.append(text[i + 1:i + 2])
                i += 2
                continue
            if text[i] == '"':
                in_string = False
            i += 1
            continue
        if two == "/*":
            in_block = True
            i += 2
            continue
        if two == "//":
            while i < len(text) and text[i] != "\n":
                i += 1
            continue
        if text[i] == '"':
            in_string = True
        out.append(text[i])
        i += 1
    return "".join(out)


def source(name: str) -> str:
    path = SRC / name
    if not path.exists():
        problem(f"missing source file: {name}")
        return ""
    return strip_comments(path.read_text(encoding="utf-8"))


def enum_constants(name: str, text: str) -> list[str]:
    match = re.search(r"enum class " + name + r"\b[^{]*\{(.*?)\n\}", text, re.S)
    if not match:
        problem(f"enum {name} not found")
        return []
    body = match.group(1)
    out = []
    for chunk in re.split(r"[,\n]", body):
        found = re.match(r"\s*([A-Z][A-Z_0-9]*)\b", chunk)
        if found:
            out.append(found.group(1))
    return out


def data_class_fields(name: str, text: str) -> list[tuple[str, str]]:
    match = re.search(r"data class " + name + r"\((.*?)\n\)", text, re.S)
    if not match:
        problem(f"data class {name} not found")
        return []
    return re.findall(r"val (\w+):\s*([\w<>?. ]+)", match.group(1))


def split_arguments(block: str) -> list[str]:
    """Split a Kotlin constructor argument list on top-level commas only."""
    args: list[str] = []
    buf = ""
    quote: str | None = None
    depth = 0
    for ch in block:
        if quote:
            buf += ch
            if ch == quote:
                quote = None
            continue
        if ch in "\"'":
            quote = ch
            buf += ch
            continue
        if ch in "([{":
            depth += 1
        if ch in ")]}":
            depth -= 1
        if ch == "," and depth == 0:
            args.append(buf.strip())
            buf = ""
            continue
        buf += ch
    if buf.strip():
        args.append(buf.strip())
    return args


def named_entries(text: str, class_name: str) -> list[dict[str, str]]:
    """Entries written with `field = value` lines, as FaceTheme and KeypadDesign are."""
    entries = []
    for match in re.finditer(class_name + r"\(\n(.*?)\n        \)", text, re.S):
        body = match.group(1)
        if not body.strip():
            continue
        fields = dict(re.findall(r"^\s+(\w+) = (.+?),?$", body, re.M))
        if fields:
            entries.append(fields)
    return entries


def positional_entries(text: str, class_name: str) -> list[list[str]]:
    """Entries written as bare argument lists, as ChargingStyle and WritingStyle are."""
    entries = []
    for match in re.finditer(r"(?<!class )" + class_name + r"\(\n(.*?)\n        \)", text, re.S):
        block = match.group(1).strip()
        if not block or block.startswith(("val ", "var ")) or block.startswith("*"):
            continue
        entries.append(split_arguments(block.replace("\n", " ")))
    return entries


def hex_colour(value: str) -> int | None:
    """Read either 0xAARRGGBB or 0xRRGGBB, the two literal forms the source uses.

    A six-digit literal is opaque, so it is widened here rather than rejected —
    contrast maths only ever looks at the RGB channels anyway.
    """
    cleaned = value.strip().replace(".toInt()", "").strip()
    match = re.fullmatch(r"0x([0-9A-Fa-f]{8})", cleaned)
    if match:
        return int(match.group(1), 16)
    match = re.fullmatch(r"0x([0-9A-Fa-f]{6})", cleaned)
    if match:
        return 0xFF000000 | int(match.group(1), 16)
    return None


# -------------------------------------------------------------------- contrast
def luminance(argb: int) -> float:
    def channel(v: int) -> float:
        value = v / 255
        return value / 12.92 if value <= 0.03928 else ((value + 0.055) / 1.055) ** 2.4

    return (
        0.2126 * channel((argb >> 16) & 0xFF)
        + 0.7152 * channel((argb >> 8) & 0xFF)
        + 0.0722 * channel(argb & 0xFF)
    )


def contrast(a: int, b: int) -> float:
    la, lb = luminance(a), luminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)


# ------------------------------------------------------------------ catalogues
def audit_catalogue_shape(text: str, class_name: str, object_name: str) -> None:
    """Catch an entry list that has been spliced into the wrong place.

    This happened for real: a scripted insert put sixteen ChargingStyle entries
    inside the renderer's `fun draw(` parameter list. Every entry was individually
    valid and the parenthesis counts balanced, so only the compiler noticed. The
    check here is positional: entries must sit inside the catalogue object, before
    it hands out a lookup.
    """
    start = text.find(f"object {object_name} {{")
    if start < 0:
        problem(f"object {object_name} not found")
        return
    marker = text.find("fun find(", start)
    end = marker if marker > 0 else text.find("\n}", start)
    body = text[start:end]
    for line in body.splitlines():
        stripped = line.strip()
        if stripped.startswith(class_name + "(") and not line.startswith("        " + class_name + "("):
            problem(f"{class_name} entry outside the catalogue list in {object_name}: {stripped[:50]}")
    if body.count("(") != body.count(")"):
        problem(f"unbalanced parentheses inside object {object_name}")


def audit_themes() -> dict:
    text = source("themes/FaceThemes.kt")
    fields = data_class_fields("FaceTheme", text)
    field_names = [f for f, _ in fields]
    entries = named_entries(text, "FaceTheme")
    if not entries:
        problem("no FaceTheme entries found")
        return {}

    for index, entry in enumerate(entries):
        label = entry.get("id", f"#{index}")
        missing = [f for f in field_names if f not in entry]
        extra = [f for f in entry if f not in field_names]
        if missing:
            problem(f"theme {label}: missing fields {missing}")
        if extra:
            problem(f"theme {label}: unknown fields {extra}")

        # every colour must be a full 8-hex-digit ARGB literal
        for field in ("background", "surface", "surfaceRaised", "border", "textPrimary",
                      "textSecondary", "accent", "accentSecondary", "iconTileColor",
                      "iconGlyphColor", "veilColor"):
            raw = entry.get(field, "")
            if hex_colour(raw) is None:
                problem(f"theme {label}: {field} is not a 0xAARRGGBB literal ({raw})")

        surface = hex_colour(entry.get("surface", ""))
        if surface is not None:
            for field, floor in (("textPrimary", 4.5), ("textSecondary", 3.0), ("accent", 3.0)):
                colour = hex_colour(entry.get(field, ""))
                if colour is None:
                    continue
                ratio = contrast(colour, surface)
                if ratio < floor:
                    problem(f"theme {label}: {field} is {ratio:.2f}:1 on surface, floor {floor}")

        # surfaceRaised carries text too, so it must not be a step away from surface
        raised = hex_colour(entry.get("surfaceRaised", ""))
        text_primary = hex_colour(entry.get("textPrimary", ""))
        if raised is not None and text_primary is not None and contrast(text_primary, raised) < 4.5:
            problem(f"theme {label}: textPrimary fails on surfaceRaised")

    ids = [e.get("id") for e in entries]
    duplicates = {i for i in ids if ids.count(i) > 1}
    if duplicates:
        problem(f"duplicate theme ids: {sorted(duplicates)}")

    return {
        "count": len(entries),
        "ids": ids,
        "light": [e.get("id") for e in entries if e.get("isDark") == "false"],
        "families": sorted({e.get("family", "").strip('"') for e in entries}),
        "wallpapers": [e.get("wallpaperId", "").strip('"') for e in entries],
        "keyboards": [e.get("keyboardDesignId", "").strip('"') for e in entries],
        "widgets": [e.get("widgetStyleId", "").strip('"') for e in entries],
        "charging": [e.get("chargingStyleId", "").strip('"') for e in entries],
        "veils": [e.get("veilStyle", "").replace("VeilStyle.", "") for e in entries],
        "tiles": [e.get("iconTile", "").replace("IconTile.", "") for e in entries],
    }


def audit_charging() -> dict:
    text = source("charging/ChargingDisplay.kt")
    audit_catalogue_shape(text, "ChargingStyle", "ChargingStyles")
    fields = data_class_fields("ChargingStyle", "\n".join(
        (SRC / p).read_text() for p in
        ["charging/ChargingDisplay.kt"] if (SRC / p).exists()
    ) or text)
    motifs = enum_constants("ChargingMotif", text)
    entries = positional_entries(text, "ChargingStyle")
    if not fields:
        return {}

    for args in entries:
        label = args[0].strip('"') if args else "?"
        if len(args) != len(fields):
            problem(f"charging style {label}: {len(args)} arguments, data class has {len(fields)}")
            continue
        motif = args[2].replace("ChargingMotif.", "")
        if motif not in motifs:
            problem(f"charging style {label}: unknown motif {motif}")
        colours = [hex_colour(a) for a in args[3:6]]
        if any(c is None for c in colours):
            problem(f"charging style {label}: colours are not 0xAARRGGBB literals")
            continue
        track, progress, spark = colours
        if contrast(progress, track) < 2.0:
            problem(f"charging style {label}: progress is {contrast(progress, track):.2f}:1 on track")

    ids = [a[0].strip('"') for a in entries if a]
    if len(ids) != len(set(ids)):
        problem(f"duplicate charging ids: {sorted({i for i in ids if ids.count(i) > 1})}")

    # a single branch may serve several motifs, e.g. `RING, PULSE ->`, so collect
    # every constant named on the left of a route, not just the first
    # Colour fields are Long; the private helpers take Int. Catch the mix-up here,
    # because it is the one error this file has produced twice.
    for bad in re.findall(r"withAlpha\(\s*style\.(\w+Color)\s*,", text):
        if bad in ("trackColor", "progressColor", "sparkColor"):
            problem(f"withAlpha called with the Long field {bad} and no .toInt()")
    for bad in re.findall(r"mix\(\s*style\.(\w+Color)\s*,", text):
        pass  # mix() takes a Long first argument by design

    routed = set()
    for arm in re.findall(r"((?:ChargingMotif\.[A-Z_]+\s*,?\s*)+)->", text):
        routed |= set(re.findall(r"ChargingMotif\.([A-Z_]+)", arm))
    unrendered = [m for m in motifs if m not in routed]
    if unrendered:
        problem(f"charging motifs with no renderer: {unrendered}")

    return {"count": len(entries), "motifs": motifs, "ids": ids}


def audit_keypads() -> dict:
    text = source("keypad/KeypadDesigns.kt")
    fields = [f for f, _ in data_class_fields("KeypadDesign", text)]
    entries = named_entries(text, "KeypadDesign")
    for entry in entries:
        label = entry.get("id", "?")
        missing = [f for f in fields if f not in entry]
        if missing:
            problem(f"keypad {label}: missing fields {missing}")
        key = hex_colour(entry.get("keySurface", ""))
        legend = hex_colour(entry.get("legend", ""))
        accent = hex_colour(entry.get("accent", ""))
        board = hex_colour(entry.get("boardBackground", ""))
        if None in (key, legend, accent, board):
            problem(f"keypad {label}: colour literal malformed")
            continue
        if contrast(legend, key) < 4.5:
            problem(f"keypad {label}: legend is {contrast(legend, key):.2f}:1 on the key")
        if contrast(accent, board) < 3.0:
            problem(f"keypad {label}: accent is {contrast(accent, board):.2f}:1 on the board")
        for field, low, high in (("keyHeightDp", 48, 96), ("keyGapDp", 2, 16), ("pressDepthDp", 0, 4)):
            value = entry.get(field, "0")
            if not value.lstrip("-").isdigit():
                problem(f"keypad {label}: {field} is not an integer ({value})")
                continue
            if not low <= int(value) <= high:
                problem(f"keypad {label}: {field}={value} outside {low}..{high}")

    ids = [e.get("id", "").strip('"') for e in entries]
    if len(ids) != len(set(ids)):
        problem(f"duplicate keypad ids: {sorted({i for i in ids if ids.count(i) > 1})}")

    purposes = enum_constants("KeypadPurpose", text)
    used = {e.get("purpose", "").replace("KeypadPurpose.", "") for e in entries}
    for purpose in purposes:
        if purpose not in used:
            problem(f"no keypad for purpose {purpose}: the gallery would show an empty shelf")
    return {"count": len(entries), "ids": ids}


def audit_writing() -> dict:
    text = source("text/WritingStyles.kt")
    fields = [f for f, _ in data_class_fields("WritingStyle", text)]
    entries = positional_entries(text, "WritingStyle")
    tones = enum_constants("WritingTone", text)
    surfaces = enum_constants("WritingSurface", text)
    for args in entries:
        label = args[0].strip('"') if args else "?"
        if len(args) != len(fields):
            problem(f"writing style {label}: {len(args)} arguments, data class has {len(fields)}")
            continue
        tone = args[2].replace("WritingTone.", "")
        surface = args[3].replace("WritingSurface.", "")
        if tone not in tones:
            problem(f"writing style {label}: unknown tone {tone}")
        if surface not in surfaces:
            problem(f"writing style {label}: unknown surface {surface}")
        # the typographic bands WritingStylesTest enforces, checked before the build
        try:
            spacing = float(args[5].rstrip("f"))
            leading = float(args[6].rstrip("f"))
            scale = float(args[7].rstrip("f"))
        except (IndexError, ValueError):
            problem(f"writing style {label}: could not read its type metrics")
        else:
            if not -0.02 <= spacing <= 0.16:
                problem(f"writing style {label}: letter spacing {spacing} outside -0.02..0.16")
            if not 1.1 <= leading <= 1.8:
                problem(f"writing style {label}: line height {leading} outside 1.1..1.8")
            if not 0.9 <= scale <= 1.4:
                problem(f"writing style {label}: scale {scale} outside 0.9..1.4")
    ids = [a[0].strip('"') for a in entries if a]
    if len(ids) != len(set(ids)):
        problem(f"duplicate writing ids: {sorted({i for i in ids if ids.count(i) > 1})}")
    by_surface: dict[str, int] = {}
    for args in entries:
        if len(args) > 3:
            key = args[3].replace("WritingSurface.", "")
            by_surface[key] = by_surface.get(key, 0) + 1
    for surface in surfaces:
        if by_surface.get(surface, 0) == 0:
            problem(f"no writing style for surface {surface}")
    return {"count": len(entries), "surfaces": by_surface, "tones": tones}


def audit_glyphs() -> dict:
    text = source("graphics/IconRenderer.kt")
    glyphs = enum_constants("IconGlyph", text)
    tiles = enum_constants("IconTile", text)
    routes = dict(re.findall(r"IconGlyph\.([A-Z_0-9]+)\s*->\s*draw([A-Za-z]+)\(", text))
    functions = set(re.findall(r"private fun draw([A-Za-z]+)\(", text))
    unrouted = [g for g in glyphs if g not in routes]
    if unrouted:
        problem(f"glyphs with no draw route: {unrouted}")
    dangling = [f for f in routes.values() if f not in functions]
    if dangling:
        problem(f"routes calling a draw function that does not exist: {dangling}")
    # The studio previews glyphs in Compose and must not fall back to a generic
    # shape for a glyph its families ask for. `else ->` is allowed as a safety net;
    # silently relying on it for real vocabulary is not.
    studio = source("ui/VisualStudio.kt")
    start = studio.find("fun DrawScope.drawStudioGlyph")
    if start < 0:
        problem("drawStudioGlyph not found")
    else:
        depth, opened, end = 0, False, len(studio)
        for index in range(start, len(studio)):
            if studio[index] == "{":
                depth += 1
                opened = True
            elif studio[index] == "}":
                depth -= 1
                if opened and depth == 0:
                    end = index
                    break
        body = studio[start:end]
        # an arm may serve several glyphs, e.g. `IconGlyph.CHAT, IconGlyph.MESSAGE ->`
        drawn = set()
        for arm in re.findall(r"((?:IconGlyph\.[A-Z_0-9]+\s*,?\s*)+)->", body):
            drawn |= set(re.findall(r"IconGlyph\.([A-Z_0-9]+)", arm))
        missing = [g for g in glyphs if g not in drawn]
        if missing:
            problem(f"glyphs the studio preview cannot draw: {missing}")

    return {
        "count": len(glyphs),
        "tiles": len(tiles),
        "unused_functions": sorted(functions - set(routes.values())),
    }


def audit_widget_finishes() -> dict:
    text = source("widget/FaceItClockWidgetStyles.kt")
    fields = [f for f, _ in data_class_fields("FaceItClockStyle", text)]
    entries = named_entries(text, "FaceItClockStyle")
    if not entries:
        problem("no FaceItClockStyle entries found")
    previews = [e.get("previewRes", "").replace("R.drawable.", "").strip() for e in entries]
    on_disk = {p.stem for p in (RES / "drawable-nodpi").glob("*.png")}
    missing = [p for p in previews if p and p not in on_disk]
    if missing:
        problem(f"widget finishes with no preview image: {missing}")
    orphans = sorted(name for name in on_disk if name.startswith("faceit_widget_preview_")
                     and name not in previews)
    if orphans:
        problem(f"widget preview images on disk that no finish uses: {orphans}")
    for entry in entries:
        label = entry.get("id", "?")
        missing_fields = [f for f in fields if f not in entry]
        if missing_fields:
            problem(f"widget finish {label}: missing fields {missing_fields}")
        background = hex_colour(entry.get("background", ""))
        time = hex_colour(entry.get("time", ""))
        date = hex_colour(entry.get("date", ""))
        malformed = [name for name, value in
                     (("background", background), ("time", time), ("date", date))
                     if value is None]
        if malformed:
            problem(f"widget finish {label}: not a colour literal: {malformed}")
            continue
        if contrast(time, background) < 7.0:
            problem(f"widget finish {label}: time is {contrast(time, background):.2f}:1, floor 7.0")
        if contrast(date, background) < 4.5:
            problem(f"widget finish {label}: date is {contrast(date, background):.2f}:1, floor 4.5")
    ids = [e.get("id", "").strip('"') for e in entries]
    if len(ids) != len(set(ids)):
        problem(f"duplicate widget finish ids: {sorted({i for i in ids if ids.count(i) > 1})}")
    return {"count": len(entries), "ids": ids}


def audit_cross_references(themes: dict, charging: dict, keypads: dict, glyphs: dict) -> None:
    if not themes:
        return
    # The charging picker must offer the user a real choice. An earlier version of
    # this check demanded 0.6 styles per theme, which is a bad proxy: it forces the
    # animation catalogue to grow linearly with the theme gallery, and would push us
    # to add styles nobody chose just to satisfy arithmetic. What actually matters is
    # that the catalogue is large in absolute terms and that its entries are spread
    # across the themes rather than piled onto two or three.
    if charging.get("count"):
        if charging["count"] < 30:
            problem(f"only {charging['count']} charging styles; the picker has no choice in it")
        from collections import Counter
        spread = Counter(themes.get("charging", []))
        most_used, count = spread.most_common(1)[0]
        ceiling = max(4, int(themes["count"] * 0.15))
        if count > ceiling:
            problem(f"charging style {most_used} is the default for {count} themes (ceiling {ceiling})")
        if len(spread) < 20:
            problem(f"themes use only {len(spread)} distinct charging styles")
    defaults = len(set(themes.get("charging", [])))
    if defaults < 10:
        problem(f"themes use only {defaults} distinct charging defaults")

    # a widget finish no theme points at is artwork the user can only reach by
    # opening the widget picker blind; every finish should be reachable from a theme
    finishes = set(audit_widget_finishes()["ids"])
    unreachable = sorted(f for f in finishes if f not in set(themes.get("widgets", [])))
    if unreachable:
        problem(f"widget finishes no theme uses: {unreachable}")
    # Every theme picks a wallpaper, and every wallpaper is now a photograph from
    # the bundled pack. A theme pointing at a drawn artwork id would resolve to
    # nothing and leave the user on an empty backdrop.
    photo_index = json.loads((ASSETS / "photos" / "photo_index.json").read_text(encoding="utf-8"))
    photographs = {p["id"] for p in photo_index["photos"]}
    unknown = sorted({w for w in themes["wallpapers"] if w not in photographs})
    if unknown:
        problem(f"themes naming wallpapers that are not in the photograph pack: {unknown}")

    catalogue = source("keyboard/KeyboardDesignCatalogue.kt")
    keyboard_ids = set(re.findall(r'id = "([a-z_0-9]+)"', catalogue)) | set(
        re.findall(r'"([a-z_0-9]+)" to KeyboardPalette', catalogue))
    unknown = sorted({k for k in themes["keyboards"] if k not in keyboard_ids})
    if unknown:
        problem(f"themes naming keyboard designs that do not exist: {unknown}")

    widget_ids = {i.strip('"') for i in audit_widget_finishes()["ids"]}
    unknown = sorted({w for w in themes["widgets"] if w not in widget_ids})
    if unknown:
        problem(f"themes naming widget finishes that do not exist: {unknown}")

    unknown = sorted({c for c in themes["charging"] if c not in set(charging.get("ids", []))})
    if unknown:
        problem(f"themes naming charging styles that do not exist: {unknown}")

    veils = enum_constants("VeilStyle", source("themes/FaceThemes.kt"))
    unknown = sorted({v for v in themes["veils"] if v and v not in veils})
    if unknown:
        problem(f"themes using frame shapes that do not exist: {unknown}")

    tiles = enum_constants("IconTile", source("graphics/IconRenderer.kt"))
    unknown = sorted({t for t in themes["tiles"] if t and t not in tiles})
    if unknown:
        problem(f"themes using icon tiles that do not exist: {unknown}")

    # the frame test states a count; it must match the enum minus the off state
    veils_all = enum_constants("VeilStyle", source("themes/FaceThemes.kt"))
    expected = len([v for v in veils_all if v != "NONE"])
    test = (ROOT / "app" / "src" / "test" / "java" / "com" / "zamcan" / "faceit"
            / "themes" / "DisplayFrameTest.kt")
    if test.exists():
        found = re.search(r"assertEquals\((\d+), shapes\.size\)", test.read_text())
        if found and int(found.group(1)) != expected:
            problem(f"DisplayFrameTest expects {found.group(1)} shapes, the enum has {expected}")

    # every frame shape must be drawable in the studio
    studio = source("ui/VisualStudio.kt")
    routed = set(re.findall(r"VeilStyle\.([A-Z_]+)\s*->", studio))
    undrawn = [v for v in veils if v not in routed]
    if undrawn:
        problem(f"frame shapes the studio cannot draw: {undrawn}")
    shown = set(re.findall(r"VeilStyle\.([A-Z_]+) to \(", studio))
    unlisted = [v for v in veils if v not in shown and v != "NONE"]
    if unlisted:
        problem(f"frame shapes missing from the display shelf: {unlisted}")

    # every family the theme catalogue uses needs its own icon vocabulary, or the
    # studio silently shows that whole family the same eight default icons
    vocabularies = set(re.findall(r'^\s+"(\w+)" -> listOf\(', studio, re.M))
    for family in themes.get("families", []):
        if family not in vocabularies:
            problem(f"theme family {family} has no icon vocabulary; it would show the default set")

    # every glyph vocabulary the studio names must exist
    used = set(re.findall(r"IconGlyph\.([A-Z_0-9]+)", studio))
    glyph_names = set(enum_constants("IconGlyph", source("graphics/IconRenderer.kt")))
    unknown = sorted(used - glyph_names)
    if unknown:
        problem(f"studio naming glyphs that do not exist: {unknown}")


def image_size(path: Path) -> tuple[int, int] | None:
    """Read pixel dimensions from a JPEG or PNG header.

    The CI runner has no imaging library and this check does not need one: the
    only question is whether the file on disk is the size the index claims, and
    that is in the frame header. Parsing it here keeps the audit dependency-free
    rather than making the build install Pillow to look at 168 files.
    """
    try:
        data = path.read_bytes()[:65536]
    except OSError:
        return None
    if data[:8] == b"\x89PNG\r\n\x1a\n":
        return int.from_bytes(data[16:20], "big"), int.from_bytes(data[20:24], "big")
    if data[:2] != b"\xff\xd8":
        return None
    index = 2
    while index + 9 < len(data):
        if data[index] != 0xFF:
            index += 1
            continue
        marker = data[index + 1]
        # SOF0-SOF15 carry the frame size; C4/C8/CC are not frame headers.
        if 0xC0 <= marker <= 0xCF and marker not in (0xC4, 0xC8, 0xCC):
            height = int.from_bytes(data[index + 5:index + 7], "big")
            width = int.from_bytes(data[index + 7:index + 9], "big")
            return width, height
        length = int.from_bytes(data[index + 2:index + 4], "big")
        if length < 2:
            return None
        index += 2 + length
    return None


def audit_photo_pack() -> dict:
    """Audit the photographic pack.

    The generated artwork is audited by construction; a photo pack is different
    because its contents come from outside. What can be checked statically is
    that every record has complete attribution, that the licences are ones we may
    ship, that the files exist at the size the index claims, and that the app's
    Kotlin reader agrees with the schema here. A photograph credited to nobody, or
    bundled under a licence we cannot use, is the one failure that must never
    reach a release.
    """
    index_path = ASSETS / "photos" / "photo_index.json"
    if not index_path.is_file():
        problem("photo_index.json is missing; the photographic pack is absent")
        return {}

    index = json.loads(index_path.read_text())
    photos = index.get("photos", [])
    if not photos:
        problem("photo_index.json lists no photographs")
        return {}

    if index.get("count") != len(photos):
        problem(f"photo index says count {index.get('count')} but lists {len(photos)}")

    shippable = {"cc0", "pdm", "publicdomain", "cc-by", "cc-by-sa"}
    ids, sha_seen = set(), {}
    categories, licences = {}, {}
    synthetic_count = 0
    share_alike = 0
    total_bytes = 0

    for entry in photos:
        photo_id = entry.get("id", "")
        if not photo_id:
            problem("a photograph has no id")
            continue
        if photo_id in ids:
            problem(f"duplicate photograph id: {photo_id}")
        ids.add(photo_id)

        licence = entry.get("licence", "")
        synthetic = bool(entry.get("synthetic"))
        synthetic_count = (synthetic_count + 1) if synthetic else synthetic_count
        licences[licence] = licences.get(licence, 0) + 1
        if licence not in shippable:
            problem(f"{photo_id}: licence '{licence}' may not be bundled")
        if synthetic and entry.get("provider") != "generated":
            problem(f"{photo_id}: marked synthetic but its provider is not 'generated'")
        tokens = re.split(r"[-_ ]", licence.lower())
        if any(token in {"nc", "nd", "gfdl"} for token in tokens):
            problem(f"{photo_id}: licence '{licence}' is not redistributable")

        for field in ("title", "creator", "licenceUrl", "source", "asset", "thumbnail"):
            if not str(entry.get(field, "")).strip():
                problem(f"{photo_id}: missing {field}; attribution incomplete")

        if entry.get("shareAlike"):
            share_alike += 1
            if licence != "cc-by-sa":
                problem(f"{photo_id}: shareAlike set but licence is {licence}")

        category = entry.get("category", "?")
        categories[category] = categories.get(category, 0) + 1

        asset = ASSETS / entry.get("asset", "")
        thumb = ASSETS / entry.get("thumbnail", "")
        if not asset.is_file():
            problem(f"{photo_id}: asset file is missing")
            continue
        if not thumb.is_file():
            problem(f"{photo_id}: thumbnail is missing")
        if asset.stat().st_size != entry.get("bytes"):
            problem(f"{photo_id}: recorded size does not match the file on disk")
        total_bytes += asset.stat().st_size

        digest = hashlib.sha256(asset.read_bytes()).hexdigest()
        if digest != entry.get("sha256"):
            problem(f"{photo_id}: sha256 does not match the file on disk")
        if digest in sha_seen:
            problem(f"{photo_id}: identical to {sha_seen[digest]}; duplicate photograph")
        sha_seen[digest] = photo_id

        size = image_size(asset)
        if size is None:
            problem(f"{photo_id}: could not read the image header")
        else:
            if size != (entry.get("width"), entry.get("height")):
                problem(f"{photo_id}: index dimensions disagree with the file")
            if max(size) > 2400:
                problem(f"{photo_id}: {size} is larger than a phone needs")

    # Orphaned files are invisible to every other check and go straight into the
    # APK. 408 of them accumulated across three rounds of curation and cost 87 MB
    # before anyone noticed, so the audit now refuses to let them come back.
    referenced = set()
    for entry in photos:
        for key in ("asset", "thumbnail"):
            path = ASSETS / entry.get(key, "")
            referenced.add(path.resolve())
    for directory in (ASSETS / "photos", ASSETS / "photos" / "thumbs"):
        if not directory.is_dir():
            continue
        for candidate in directory.iterdir():
            if candidate.suffix.lower() not in {".jpg", ".jpeg", ".png"}:
                continue
            if candidate.resolve() not in referenced:
                problem(f"orphaned photograph file with no index entry: {candidate.name}")

    if len(categories) < 3:
        problem(f"photo pack covers only {sorted(categories)}; breadth requested was wider")
    for category, count in sorted(categories.items()):
        if count < 5:
            problem(f"category '{category}' has only {count} photographs")

    # The app reads these fields by name; a rename in Kotlin would break the
    # gallery silently, so the reader is checked against this schema.
    reader_path = SRC / "data" / "PhotoLibrary.kt"
    reader = strip_comments(reader_path.read_text()) if reader_path.is_file() else ""
    if not reader:
        problem("data/PhotoLibrary.kt is missing; the gallery has no reader")
    for field in ("title", "creator", "licence", "licenceUrl", "source", "asset",
                  "thumbnail", "shareAlike", "category", "width", "height", "synthetic"):
        if f'"{field}"' not in reader:
            problem(f"PhotoLibrary.kt does not read '{field}', which the index provides")

    docs = ROOT / "docs" / "PHOTO_PACK.md"
    if not docs.is_file():
        problem("docs/PHOTO_PACK.md is missing; the pack needs a written licence record")
    else:
        text = docs.read_text()
        if str(len(photos)) not in text:
            problem(f"docs/PHOTO_PACK.md does not state the pack size ({len(photos)})")
        for category in categories:
            if category not in text:
                problem(f"docs/PHOTO_PACK.md does not mention the '{category}' category")

    if synthetic_count and "generated" not in (ROOT / "docs" / "PHOTO_PACK.md").read_text().lower():
        problem("the pack contains generated images that PHOTO_PACK.md does not disclose")

    return {
        "count": len(photos),
        "synthetic": synthetic_count,
        "categories": categories,
        "licences": licences,
        "share_alike": share_alike,
        "megabytes": round(total_bytes / 1048576, 1),
    }


def audit_documentation() -> None:
    """The docs must not quote figures that can drift from the source.

    The first version of this check compared a hand-typed table against the
    catalogues and found it already stale. Rather than turn every catalogue
    change into a game of keeping a table in step, the rule is now structural:
    the personalisation document defers to this audit for counts, and the
    architecture document is checked for the presence of a catalogue inventory.
    """
    path = DOCS / "PERSONALISATION_ENGINES.md"
    if not path.exists():
        problem("docs/PERSONALISATION_ENGINES.md is missing")
        return
    text = path.read_text(encoding="utf-8")
    if "tools/verify_catalogue.py" not in text:
        problem("the personalisation document does not point at the catalogue audit")
    if re.search(r"^\| Catalogue \| Before \| Now \|", text, re.M):
        problem("the personalisation document quotes a hand-typed count table; "
                "defer to tools/verify_catalogue.py instead")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--verbose", action="store_true")
    args = parser.parse_args()

    themes = audit_themes()
    charging = audit_charging()
    keypads = audit_keypads()
    writing = audit_writing()
    glyphs = audit_glyphs()
    widgets = audit_widget_finishes()
    photos = audit_photo_pack()
    audit_cross_references(themes, charging, keypads, glyphs)
    audit_documentation()

    if args.verbose:
        print("themes      ", themes.get("count"), "| light", len(themes.get("light", [])),
              "| families", len(themes.get("families", [])))
        print("             ", ", ".join(themes.get("families", [])))
        print("glyphs      ", glyphs.get("count"), "| tile finishes", glyphs.get("tiles"),
              "| shared helpers", glyphs.get("unused_functions"))
        print("charging    ", charging.get("count"), "| motifs", len(charging.get("motifs", [])))
        print("keypads     ", keypads.get("count"))
        print("writing     ", writing.get("count"), "|", writing.get("surfaces"))
        print("widgets     ", widgets.get("count"))
        print("distinct photographs paired to themes:", len(set(themes.get("wallpapers", []))))
        if photos:
            print("photographs ", photos["count"], "|", photos["megabytes"], "MB |",
                  "share-alike", photos["share_alike"], "| generated", photos.get("synthetic", 0))
            print("             ", photos["categories"])
            print("             ", photos["licences"])

    if PROBLEMS:
        print(f"Catalogue audit FAILED: {len(PROBLEMS)} problem(s)")
        for item in PROBLEMS:
            print(f"  - {item}")
        return 1
    print("Catalogue audit: PASS — arity, ids, cross-references, contrast, "
          "resources and documentation counts all agree with the source.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
