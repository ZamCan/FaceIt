#!/usr/bin/env python3
"""Validate FaceIt's bundled artwork, dimensions, hashes and rights metadata."""

from __future__ import annotations

import argparse
import hashlib
import json
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "content" / "artwork" / "manifest.json"
WALLPAPER_DIR = ROOT / "app" / "src" / "main" / "assets" / "wallpapers"
OBJECT_DIR = ROOT / "app" / "src" / "main" / "assets" / "objects"
MIN_WALLPAPERS = 100
MIN_WALLPAPER_EDGE = 1080


def png_dimensions(path: Path) -> tuple[int, int]:
    with path.open("rb") as stream:
        header = stream.read(24)
    if len(header) != 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise ValueError("not a valid PNG header")
    return struct.unpack(">II", header[16:24])


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--release", action="store_true", help="require the complete 100-wallpaper release pack")
    args = parser.parse_args()

    if not MANIFEST.exists():
        print(f"ERROR: missing {MANIFEST.relative_to(ROOT)}")
        return 1

    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    entries = manifest.get("assets", [])
    if not isinstance(entries, list):
        print("ERROR: manifest assets must be an array")
        return 1

    errors: list[str] = []
    seen_ids: set[str] = set()
    seen_paths: set[str] = set()
    wallpaper_count = 0

    for item in entries:
        asset_id = str(item.get("id", "")).strip()
        relative = str(item.get("path", "")).strip()
        if not asset_id or asset_id in seen_ids:
            errors.append(f"missing or duplicate asset id: {asset_id!r}")
            continue
        seen_ids.add(asset_id)
        if not relative or relative.startswith("/") or ".." in Path(relative).parts:
            errors.append(f"{asset_id}: unsafe or missing path")
            continue
        if relative in seen_paths:
            errors.append(f"{asset_id}: duplicate path {relative}")
            continue
        seen_paths.add(relative)

        category = item.get("category")
        if not category:
            errors.append(f"{asset_id}: missing category")
        for field in ("title", "creator", "license", "source", "sha256"):
            if not str(item.get(field, "")).strip():
                errors.append(f"{asset_id}: missing rights/provenance field {field}")

        file_path = (ROOT / relative).resolve()
        if ROOT not in file_path.parents or not file_path.is_file():
            errors.append(f"{asset_id}: file does not exist: {relative}")
            continue
        if file_path.suffix.lower() != ".png":
            errors.append(f"{asset_id}: bundled source must be PNG")
            continue

        try:
            width, height = png_dimensions(file_path)
        except (OSError, ValueError, struct.error) as error:
            errors.append(f"{asset_id}: {error}")
            continue

        if item.get("sha256") != sha256(file_path):
            errors.append(f"{asset_id}: SHA-256 mismatch")
        if item.get("type") == "wallpaper":
            wallpaper_count += 1
            if min(width, height) < MIN_WALLPAPER_EDGE:
                errors.append(f"{asset_id}: wallpaper resolution {width}x{height} is below 1080px on its short edge")

    if args.release and wallpaper_count < MIN_WALLPAPERS:
        errors.append(f"release pack has {wallpaper_count} wallpapers; at least {MIN_WALLPAPERS} are required")

    print(f"FaceIt artwork validation: {len(entries)} manifest entries, {wallpaper_count} wallpapers")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    if not args.release and wallpaper_count < MIN_WALLPAPERS:
        print(f"CONTENT GATE: release pack is not complete ({MIN_WALLPAPERS} wallpapers required).")
    else:
        print("PASS: artwork manifest, files, hashes and metadata are valid.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
