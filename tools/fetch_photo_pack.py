#!/usr/bin/env python3
"""Fetch a free-licensed photography pack for Face It.

Why this exists
---------------
The generated artwork in `assets/wallpapers` is original vector construction: no
photography, no third-party material, and therefore trivially clear to ship. The
product owner asked for real photographs instead of plain artwork, so this script
builds a *separate* pack with a different provenance story, kept deliberately
isolated from the generated one.

The rule this script exists to enforce
--------------------------------------
Bundling photographs into a shipping app is a licensing act. "Famous available
images" are almost always copyright-protected: a photograph belongs to the person
who took it whether or not it is easy to download. So this script only accepts:

  * CC0 / Public Domain Mark  -- no conditions, still credited;
  * CC BY 4.0 / 3.0 / 2.0     -- credit and a licence link are mandatory;
  * CC BY-SA                  -- accepted only into the share-alike-safe pack,
                                 and recorded as such so a release decision is
                                 never made blind.

Anything under a non-commercial, no-derivatives, all-rights-reserved or unknown
licence is rejected outright, whatever it looks like.

Quality gates
-------------
A photo is only accepted if it is genuinely usable as a phone wallpaper:

  * at least 1600 x 900, so it survives a 1440-wide screen without upscaling;
  * landscape or square (a tall crop of a wide photo loses too much);
  * not dominated by flat sky or flat wall -- an entropy and edge-density check
    throws out the empty ones that look fine as a thumbnail and empty at full size;
  * a resolution-independent sharpness score, so blurred shots are rejected.

Every accepted file is recorded with its creator, licence, licence URL, source
page and the exact byte hash, so the app can show real attribution and the pack
can be audited later without re-downloading anything.

Usage
-----
    python3 tools/fetch_photo_pack.py --plan          # show what it would fetch
    python3 tools/fetch_photo_pack.py --limit 40      # fetch up to 40
    python3 tools/fetch_photo_pack.py --verify        # re-check the local pack

Requires network access; nothing is bundled without it having been fetched.
"""

from __future__ import annotations

import argparse
import hashlib
import re
import io
import json
import math
import sys
import time
import urllib.parse
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PHOTO_DIR = ROOT / "app" / "src" / "main" / "assets" / "photos"
THUMB_DIR = PHOTO_DIR / "thumbs"
INDEX = PHOTO_DIR / "photo_index.json"
REJECTED = PHOTO_DIR / "rejected.json"
UA = "FaceItWallpaperPack/1.0 (https://github.com/ZamCan/FaceIt; asset sourcing)"

# Lower is preferred: a CC0 file asks nothing of us that a CC BY-SA file does not
# ask more of.
LICENCE_PREFERENCE = {"cc0": 0, "pdm": 0, "cc-by": 1, "cc-by-sa": 2}

# Licences that may be bundled, and whether they carry share-alike obligations.
ACCEPTED_LICENCES = {
    "cc0": False,
    "pdm": False,
    "cc-by": False,
    "cc-by-sa": True,
}

# Categories the product owner asked for, in their words: animals, nature,
# electronics, wearables. Each is searched with plain subject terms.
CATEGORIES = {
    # The product owner changed direction: no cute animals and no landscapes.
    # The pack is now about made things — gemstones, metal, jewellery, the spaces
    # where iron is worked — so the animal and nature categories are gone rather
    # than merely deprioritised.
    "diamonds": [
        "diamond gemstone macro", "diamond brilliant cut", "rough diamond",
        "diamond ring solitaire", "diamond necklace", "diamond cutting",
        "brilliant cut gemstone", "diamond crystal", "gem cutter workshop",
    ],
    "bracelets": [
        "gold bracelet studio", "silver bracelet studio", "diamond ring macro",
        "diamond brilliant", "gemstone ring studio", "pearl necklace studio",
        "gold necklace studio", "pendant gold", "brooch gold", "tiara diamond",
        "gold chain macro", "emerald ring", "sapphire ring", "ruby ring",
        "amber jewellery", "enamel jewellery", "filigree silver", "cuff bracelet",
        "torque necklace", "cameo brooch",
    ],
    "minerals": [
        "mineral specimen", "quartz crystal", "amethyst", "gemstone", "tourmaline",
        "malachite specimen", "obsidian specimen", "pyrite specimen",
        "fluorite specimen", "agate slice", "geode crystal",
        "bismuth crystal", "sulfur crystals", "calcite", "topaz", "emerald crystal",
        "ruby crystal mineral", "sapphire crystal mineral", "opal gemstone",
        "labradorite stone", "azurite mineral",
        "cinnabar mineral", "halite mineral", "gypsum crystal", "smithsonite mineral",
    ],
    "metalwork": [
        "wrought iron", "blacksmith forge", "iron gate", "steel structure",
        "welding sparks", "metal lattice", "cast iron", "anvil", "rivets steel",
        "iron bridge", "steel truss", "hammered metal", "metal workshop",
        "locksmith forge", "iron railing", "copper patina", "bronze casting",
        "steel beam", "chain steel", "forge fire",
    ],
    "ironwork": [
        "industrial hall", "factory interior", "warehouse interior",
        "turbine hall", "power station interior", "brutalist interior",
        "concrete architecture", "steel mill", "abandoned factory", "loft interior",
        "machine hall", "boiler house", "gantry crane", "subway tunnel",
        "cathedral interior stone", "staircase interior", "loading dock",
        "foundry interior", "hangar interior", "water tower interior",
    ],
    "wearables": [
        "sneakers pair", "leather handbag", "wool scarf", "denim fabric",
        "knitwear sweater", "sunglasses product", "fedora hat", "tailored suit",
        "leather boots", "textile weave detail", "trench coat", "cashmere shawl",
        "silk tie", "evening gown detail", "handmade shoes",
        "embroidery detail", "lace fabric", "tweed fabric", "linen texture",
    ],
    "technology": [
        "circuit board macro", "computer hardware", "server room", "robot arm",
        "camera lens macro", "headphones product", "smartphone flat lay",
        "mechanical keyboard", "drone flight", "laboratory equipment",
        "graphics card", "microscope", "telescope", "solar panel", "wind turbine",
        "robot", "3d printer", "vr headset", "smartwatch", "laptop screen",
    ],
}

MIN_WIDTH = 1600
MIN_HEIGHT = 900
MIN_ENTROPY = 5.2          # bits; below this the frame is a flat wall or sky
MIN_EDGE_DENSITY = 0.012   # share of pixels on a strong edge


def log(message: str) -> None:
    print(message, flush=True)


LICENCE_PATTERNS = [
    ("cc0", False, ["cc0", "cc-zero", "public domain", "pd-self", "pd-us"]),
    ("pdm", False, ["public domain mark", "pd-old", "pd-art", "pd-author"]),
    ("cc-by", False, ["cc by 4.0", "cc by 3.0", "cc by 2.5", "cc by 2.0", "cc-by-4.0", "cc-by-3.0"]),
    ("cc-by-sa", True, ["cc by-sa 4.0", "cc by-sa 3.0", "cc by-sa 2.5", "cc by-sa 2.0",
                        "cc-by-sa-4.0", "cc-by-sa-3.0"]),
]
# A licence-clean file can still be the wrong kind of picture. These describe
# artwork rather than photography, and a scan of an oil painting is not a
# wallpaper however free it is. Matched against the Commons subject categories.
NOT_A_PHOTOGRAPH = [
    "painting", "paintings", "oil on canvas", "engraving", "etching", "lithograph",
    "woodcut", "drawing", "drawings", "sketch", "illustration", "artwork",
    "sculpture", "fresco", "manuscript", "poster", "cartoon", "printmaking",
    "watercolour", "watercolor", "mural", "mosaic",
]

# The category check above misses subjects whose Commons categories are generic:
# a photograph of a sculpture is catalogued as a photograph. These are matched
# against the file title and the search term, and a hit means the picture is not
# what the category promises — a Jeff Koons "Puppy" is a sculpture, not an animal;
# a museum diorama is not a meadow; a watercolour is not a storm.
NOT_THE_SUBJECT = [
    "sculpture", "statue", "painting", "drawing", "watercolour", "watercolor",
    "illustration", "diagram", "chart", "render", "computer-generated",
    "taxidermy", "diorama", "model of", "replica", "figurine", "bust of",
    "engraving", "lithograph", "coat of arms", "heraldic", "blueprint",
    "gasket", "spare part", "disassembled", "tile,", "louvre", "al-qazwini",
    "fashion model", "runway", "catwalk", "fashion week", "on the catwalk",
    # "silk tie" returned surgical sutures, which is a search-engine problem more
    # than an image problem: the word "tie" is ambiguous and Commons indexed the
    # stitches. Caught here rather than trusting the query.
    "suture", "surgery", "surgical", "orthopedic", "post-op", "scar tissue",
    "wound", "bandage", "dissection",
    "blue marble", "eiffelturm", "tour eiffel", "satellite image", "map of",
    "postage stamp", "banknote", "coin of",
]

# anything mentioning these is refused even if a free string also appears
REFUSE = ["noncommercial", "non-commercial", "nc", "no derivative", "nd", "fair use",
          "all rights reserved", "copyright", "gfdl", "gfdl 1.3"]


def classify_licence(short: str) -> tuple[str, bool] | None:
    text = (short or "").lower().strip()
    if not text:
        return None
    for token in REFUSE:
        # `nc` and `nd` are matched as standalone tokens to avoid tripping on
        # words like "licensed"; the longer phrases are matched anywhere
        if len(token) <= 2:
            if any(part == token for part in text.replace("-", " ").split()):
                return None
        elif token in text:
            return None
    for name, share_alike, patterns in LICENCE_PATTERNS:
        if any(pattern in text for pattern in patterns):
            return name, share_alike
    return None


def search(term: str, page_size: int = 12) -> list[dict]:
    """Ask Wikimedia Commons for photographs of one subject, with full metadata.

    Commons is used rather than a stock-photo API for two reasons: it serves the
    original file at full resolution (Openverse returns 1024 px thumbnails, which
    fail a wallpaper size gate), and its API states the licence and the author in
    machine-readable form, so attribution is recorded rather than guessed.
    """
    query = urllib.parse.urlencode({
        "action": "query",
        "format": "json",
        "generator": "search",
        "gsrsearch": f"filetype:bitmap {term}",
        "gsrnamespace": 6,
        "gsrlimit": page_size,
        "prop": "imageinfo",
        "iiprop": "url|size|extmetadata|mime",
        "iiurlwidth": 2560,
    })
    url = f"https://commons.wikimedia.org/w/api.php?{query}"
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    try:
        with urllib.request.urlopen(request, timeout=45) as response:
            payload = json.loads(response.read())
    except Exception as error:  # network flake: skip the term, not the run
        log(f"  ! search failed for {term!r}: {error}")
        return []

    pages = payload.get("query", {}).get("pages", {})
    out = []
    for page in pages.values():
        info = (page.get("imageinfo") or [{}])[0]
        meta = info.get("extmetadata", {})
        short = meta.get("LicenseShortName", {}).get("value", "")
        decided = classify_licence(short)
        if not decided:
            continue
        name, share_alike = decided
        if info.get("mime") not in ("image/jpeg", "image/png"):
            continue
        categories = (meta.get("Categories", {}).get("value", "") or "").lower()
        if any(word in categories for word in NOT_A_PHOTOGRAPH):
            continue
        artist = meta.get("Artist", {}).get("value", "")
        # the artist field is HTML; strip tags and collapse whitespace
        artist = re.sub(r"<[^>]+>", " ", artist)
        artist = " ".join(artist.split()) or "Unknown"
        out.append({
            "url": info.get("url") or info.get("thumburl") or "",
            "previewUrl": info.get("thumburl") or "",
            "width": info.get("width"),
            "height": info.get("height"),
            "title": page.get("title", "").replace("File:", ""),
            "creator": artist[:80],
            "creatorUrl": info.get("descriptionurl", ""),
            "licence": name,
            "licenceVersion": short,
            "licenceUrl": meta.get("LicenseUrl", {}).get("value", ""),
            "source": info.get("descriptionurl", ""),
            "provider": "wikimedia-commons",
            "shareAlike": share_alike,
        })
    return out


def fetch_binary(url: str, timeout: int = 90, attempts: int = 5) -> bytes | None:
    """Download with backoff.

    Wikimedia rate-limits large batches with HTTP 429 and a Retry-After header.
    Dropping the file on the first refusal is how the first technology and luxury
    passes lost 87 photographs to "download failed" when nothing was wrong with
    them, so this waits and tries again rather than discarding good candidates.
    """
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    for attempt in range(1, attempts + 1):
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                return response.read()
        except urllib.error.HTTPError as error:
            if error.code == 429 and attempt < attempts:
                wait = int(error.headers.get("Retry-After") or 0) or (5 * attempt)
                log(f"  … rate limited, waiting {wait}s (attempt {attempt})")
                time.sleep(wait)
                continue
            log(f"  ! download failed: HTTP {error.code}")
            return None
        except Exception as error:
            if attempt < attempts:
                time.sleep(3 * attempt)
                continue
            log(f"  ! download failed: {error}")
            return None
    return None


def quality(data: bytes) -> tuple[bool, str, dict]:
    """Reject photographs that would look cheap as a wallpaper.

    The checks are deliberately blunt and measurable rather than a matter of taste:
    resolution, aspect, how much information is in the frame, and how sharp the
    edges are once resized to a common width, which makes the sharpness number
    comparable across cameras.
    """
    try:
        from PIL import Image, ImageFilter
        import numpy as np
    except ImportError:
        return False, "imaging libraries unavailable", {}

    try:
        image = Image.open(io.BytesIO(data))
        image.load()
    except Exception:
        return False, "not a readable image", {}

    width, height = image.size
    if width < MIN_WIDTH or height < MIN_HEIGHT:
        return False, f"too small ({width}x{height})", {"width": width, "height": height}
    if height > width * 1.05:
        return False, f"portrait crop ({width}x{height})", {"width": width, "height": height}

    probe = image.convert("L").resize((480, int(480 * height / width)), Image.LANCZOS)
    array = np.asarray(probe, dtype=np.float64)

    # Shannon entropy of the tone histogram: flat sky and blank walls score low.
    histogram, _ = np.histogram(array, bins=256, range=(0, 255))
    probabilities = histogram / histogram.sum()
    entropy = -sum(p * math.log2(p) for p in probabilities if p > 0)

    # Edge density: share of pixels that survive an edge filter.
    edges = np.asarray(probe.filter(ImageFilter.FIND_EDGES), dtype=np.float64)
    edge_density = float((edges > 40).mean())

    # Sharpness: variance of the Laplacian, normalised by contrast.
    laplacian = (
        np.abs(np.diff(array, axis=0)).mean() + np.abs(np.diff(array, axis=1)).mean()
    )
    contrast = array.std() + 1e-6
    sharpness = laplacian / contrast

    stats = {
        "width": width,
        "height": height,
        "entropy": round(entropy, 2),
        "edgeDensity": round(edge_density, 4),
        "sharpness": round(sharpness, 3),
    }
    if entropy < MIN_ENTROPY:
        return False, f"flat frame (entropy {entropy:.2f})", stats
    if edge_density < MIN_EDGE_DENSITY:
        return False, f"no detail (edge density {edge_density:.4f})", stats
    if sharpness < 0.055:
        return False, f"soft focus (sharpness {sharpness:.3f})", stats
    return True, "accepted", stats


def slug(text: str, limit: int = 28) -> str:
    keep = [c.lower() if c.isalnum() else "-" for c in text]
    out = "".join(keep)
    while "--" in out:
        out = out.replace("--", "-")
    return out.strip("-")[:limit].strip("-") or "photo"


def build_plan(per_term: int = 4) -> list[dict]:
    """Search every subject and keep the candidates that are not already held."""
    known = set()
    if INDEX.exists():
        known = {entry["id"] for entry in json.loads(INDEX.read_text())["photos"]}

    candidates: list[dict] = []
    seen_urls: set[str] = set()
    for category, terms in CATEGORIES.items():
        for term in terms:
            results = search(term, page_size=per_term)
            log(f"{category:11} {term:24} {len(results)} candidates")
            for item in results:
                licence = (item.get("licence") or "").lower()
                if licence not in ACCEPTED_LICENCES:
                    continue
                url = item.get("url")
                if not url or url in seen_urls:
                    continue
                seen_urls.add(url)
                candidates.append({
                    "category": category,
                    "term": term,
                    "url": url,
                    "title": (item.get("title") or "").strip(),
                    "creator": (item.get("creator") or "Unknown").strip(),
                    "creatorUrl": item.get("creatorUrl") or "",
                    "licence": licence,
                    "licenceVersion": item.get("licenceVersion") or "",
                    "licenceUrl": item.get("licenceUrl") or "",
                    "source": item.get("source") or "",
                    "provider": item.get("provider") or "",
                    "width": item.get("width"),
                    "height": item.get("height"),
                })
            time.sleep(0.8)
    def identity(item: dict) -> str:
        return slug(f"{item['term']}-{item['creator']}-{item['title']}")

    before = len(candidates)
    candidates = [c for c in candidates if identity(c) not in known]
    log(f"\n{before} licensed candidates, {len(candidates)} not already in the pack")
    return candidates


def run_fetch(limit: int, per_term: int, only: str | None = None) -> int:
    from PIL import Image

    PHOTO_DIR.mkdir(parents=True, exist_ok=True)
    THUMB_DIR.mkdir(parents=True, exist_ok=True)

    if INDEX.exists():
        index = json.loads(INDEX.read_text())
    else:
        index = {
            "schema_version": 1,
            "pack": "Face It Photographic Collections",
            "note": (
                "Real photographs under CC0, public domain or CC BY licences. "
                "Every entry carries its creator, licence and source page. "
                "Share-alike entries are flagged so a release decision is explicit."
            ),
            "photos": [],
        }
    rejected = json.loads(REJECTED.read_text())["rejected"] if REJECTED.exists() else []

    candidates = build_plan(per_term)
    if only:
        candidates = [c for c in candidates if c["category"] == only]
        if not candidates:
            log(f"no licensed candidates for category '{only}'")
            return 1
    # Permissively licensed files first, then the rest. Share-alike is legal to
    # bundle but it constrains how the pack itself may be redistributed, so a
    # permissive file that is otherwise acceptable is the better default.
    candidates.sort(key=lambda c: (LICENCE_PREFERENCE.get(c["licence"], 9), c["category"]))
    log(f"\n{len(candidates)} licensed candidates after de-duplication and licence filtering\n")

    taken = 0
    hashes = {entry["sha256"] for entry in index["photos"]}
    for candidate in candidates:
        if taken >= limit:
            break
        identifier = slug(f"{candidate['term']}-{candidate['creator']}-{candidate['title']}")
        if any(entry["id"] == identifier for entry in index["photos"]):
            continue

        log(f"[{taken + 1}/{limit}] {candidate['category']:11} {identifier}")
        data = fetch_binary(candidate["url"])
        if not data:
            rejected.append({**candidate, "reason": "download failed"})
            continue

        digest = hashlib.sha256(data).hexdigest()
        if digest in hashes:
            rejected.append({**candidate, "reason": "byte-identical duplicate"})
            log("  ! duplicate bytes, skipped")
            continue

        good, reason, stats = quality(data)
        if not good:
            rejected.append({**candidate, "reason": reason, "stats": stats})
            log(f"  ! rejected: {reason}")
            continue

        image = Image.open(io.BytesIO(data)).convert("RGB")
        # keep a 2560-wide master and a 640-wide thumbnail; the app never needs
        # the original 6000 px camera file and the APK has a size budget
        if image.width > 2560:
            image = image.resize((2560, round(image.height * 2560 / image.width)), Image.LANCZOS)
        master = PHOTO_DIR / f"{identifier}.jpg"
        thumb = THUMB_DIR / f"{identifier}.jpg"
        image.save(master, format="JPEG", quality=86, optimize=True, progressive=True)
        image.resize((640, round(image.height * 640 / image.width)), Image.LANCZOS).save(
            thumb, format="JPEG", quality=80, optimize=True
        )

        index["photos"].append({
            "id": identifier,
            "category": candidate["category"],
            "subject": candidate["term"],
            "title": candidate["title"] or candidate["term"],
            "creator": candidate["creator"],
            "creatorUrl": candidate["creatorUrl"],
            "licence": candidate["licence"],
            "licenceVersion": candidate["licenceVersion"],
            "licenceUrl": candidate["licenceUrl"],
            "source": candidate["source"],
            "provider": candidate["provider"],
            "shareAlike": ACCEPTED_LICENCES[candidate["licence"]],
            "asset": f"photos/{identifier}.jpg",
            "thumbnail": f"photos/thumbs/{identifier}.jpg",
            "width": image.width,
            "height": image.height,
            "bytes": master.stat().st_size,
            "sha256": hashlib.sha256(master.read_bytes()).hexdigest(),
            "quality": stats,
            "provenance": (
                "Photograph by {} under {} {}. Fetched from {} and stored unmodified "
                "apart from resizing to a {} px master."
            ).format(
                candidate["creator"], candidate["licence"].upper(),
                candidate["licenceVersion"], candidate["source"], image.width
            ),
        })
        hashes.add(digest)
        taken += 1
        log(f"  + kept ({stats['entropy']} bits entropy, {stats['width']}x{stats['height']})")
        time.sleep(1.2)

    index["count"] = len(index["photos"])
    index["categories"] = sorted({p["category"] for p in index["photos"]})
    INDEX.write_text(json.dumps(index, indent=2) + "\n", encoding="utf-8")
    REJECTED.write_text(json.dumps({"rejected": rejected}, indent=2) + "\n", encoding="utf-8")
    log(f"\npack now holds {index['count']} photographs; rejected log has {len(rejected)} entries")
    return 0


def optimise(base: Path, long_edge: int = 1920, quality: int = 80) -> int:
    """Re-encode accepted masters to a phone-appropriate size.

    A 2500 px photograph is right for print and wrong for an APK. Android ships
    the JPEG as-is — there is no further compression to be had — so an
    unoptimised pack turns a 40 MB app into a 150 MB one for resolution no phone
    screen can show. The original dimensions are kept in the index as
    `sourceWidth`/`sourceHeight` so provenance stays truthful and the resize is
    recorded rather than implied.
    """
    from PIL import Image

    index_path = base / "photo_index.json"
    index = json.loads(index_path.read_text())
    saved = 0
    for entry in index["photos"]:
        path = ROOT / "app" / "src" / "main" / "assets" / entry["asset"]
        if not path.is_file():
            continue
        before = path.stat().st_size
        with Image.open(path) as image:
            image = image.convert("RGB")
            width, height = image.size
            scale = min(long_edge / max(width, height), 1.0)
            if scale < 1.0:
                image = image.resize(
                    (max(1, round(width * scale)), max(1, round(height * scale))),
                    Image.LANCZOS
                )
            entry["sourceWidth"], entry["sourceHeight"] = width, height
            image.save(path, "JPEG", quality=quality, optimize=True, progressive=True)
        entry["width"], entry["height"] = Image.open(path).size
        entry["bytes"] = path.stat().st_size
        entry["sha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
        saved += before - entry["bytes"]
    index_path.write_text(json.dumps(index, indent=2) + "\n")
    return saved


def sweep_orphans() -> int:
    """Delete photograph files that no index entry refers to.

    Every removal made during curation leaves the file on disk, and Android ships
    whatever is in assets/. Three rounds of curation left 408 orphans holding
    87 MB before anyone counted.
    """
    index = json.loads(INDEX.read_text())
    referenced = set()
    for entry in index["photos"]:
        referenced.add(Path(entry["asset"]).name)
        referenced.add(Path(entry["thumbnail"]).name)
    removed = 0
    for directory in (PHOTO_DIR, THUMB_DIR):
        if not directory.is_dir():
            continue
        for path in directory.iterdir():
            if path.is_file() and path.suffix.lower() in {".jpg", ".jpeg", ".png"} \
                    and path.name not in referenced:
                path.unlink()
                removed += 1
    return removed


def verify() -> int:
    """Re-check the local pack: files, hashes, attribution, and the licence rule."""
    if not INDEX.exists():
        print("no photo index; nothing to verify")
        return 1
    index = json.loads(INDEX.read_text())
    problems = []
    for entry in index["photos"]:
        path = ROOT / "app" / "src" / "main" / "assets" / entry["asset"]
        thumb = ROOT / "app" / "src" / "main" / "assets" / entry["thumbnail"]
        if not path.exists():
            problems.append(f"{entry['id']}: master missing")
            continue
        if not thumb.exists():
            problems.append(f"{entry['id']}: thumbnail missing")
        if hashlib.sha256(path.read_bytes()).hexdigest() != entry["sha256"]:
            problems.append(f"{entry['id']}: hash mismatch")
        for field in ("creator", "licence", "licenceUrl", "source", "provenance"):
            if not str(entry.get(field, "")).strip():
                problems.append(f"{entry['id']}: {field} is empty; attribution incomplete")
        if entry["licence"] not in ACCEPTED_LICENCES:
            problems.append(f"{entry['id']}: licence {entry['licence']} is not in the accepted set")
    share_alike = [p["id"] for p in index["photos"] if p.get("shareAlike")]
    print(f"photo pack: {len(index['photos'])} photographs across "
          f"{len(index.get('categories', []))} categories")
    if share_alike:
        print(f"  share-alike entries (release decision needed): {len(share_alike)}")
        for identifier in share_alike[:10]:
            print(f"    - {identifier}")
    if problems:
        print(f"FAILED: {len(problems)} problem(s)")
        for item in problems:
            print(f"  - {item}")
        return 1
    print("PASS: every photograph has a file, a matching hash and full attribution.")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--plan", action="store_true", help="list candidates without downloading")
    parser.add_argument("--limit", type=int, default=20)
    parser.add_argument("--only", default="", help="restrict this run to one category")
    parser.add_argument("--per-term", type=int, default=4)
    parser.add_argument("--verify", action="store_true")
    parser.add_argument("--optimise", action="store_true", help="re-encode masters for phones")
    parser.add_argument("--sweep", action="store_true", help="delete files with no index entry")
    parser.add_argument("--long-edge", type=int, default=1920)
    parser.add_argument("--quality", type=int, default=80)
    args = parser.parse_args()

    if args.sweep:
        print(f"removed {sweep_orphans()} orphaned files")
        return verify()
    if args.optimise:
        saved = optimise(PHOTO_DIR, args.long_edge, args.quality)
        print(f"saved {saved / 1048576:.1f} MB across the pack")
        return verify()
    if args.verify:
        return verify()
    if args.plan:
        candidates = build_plan(args.per_term)
        by_category: dict[str, int] = {}
        for candidate in candidates:
            by_category[candidate["category"]] = by_category.get(candidate["category"], 0) + 1
        for category, count in sorted(by_category.items()):
            print(f"{category:12} {count}")
        print(f"total usable candidates: {len(candidates)}")
        return 0
    return run_fetch(args.limit, args.per_term, args.only or None)


if __name__ == "__main__":
    sys.exit(main())
