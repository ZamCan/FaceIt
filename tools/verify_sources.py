#!/usr/bin/env python3
"""Static sanity checks for the Face It source tree.

This is not a compiler. It catches the mistakes that a Kotlin compiler would
catch first — unbalanced delimiters, imports of source symbols that no longer
exist, R.* references with no matching resource, and duplicate top-level
declarations — so agent edits can be self-checked in an environment without a
JDK/Android SDK. It never claims a build succeeded.
"""
from __future__ import annotations

import re
import sys
from collections import defaultdict
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app" / "src" / "main" / "java"
RES = ROOT / "app" / "src" / "main" / "res"

errors: list[str] = []
notes: list[str] = []


def strip_comments_and_strings(text: str) -> str:
    out, i, n = [], 0, len(text)
    while i < n:
        two = text[i:i + 2]
        if two == "//":
            i = text.find("\n", i)
            if i < 0:
                break
        elif two == "/*":
            end = text.find("*/", i + 2)
            i = n if end < 0 else end + 2
        elif text[i:i + 3] == '"""':
            end = text.find('"""', i + 3)
            i = n if end < 0 else end + 3
        elif text[i] == '"':
            i += 1
            while i < n and text[i] != '"':
                i += 2 if text[i] == "\\" else 1
            i += 1
        elif text[i] == "'":
            i += 1
            while i < n and text[i] != "'":
                i += 2 if text[i] == "\\" else 1
            i += 1
        else:
            out.append(text[i])
            i += 1
    return "".join(out)


def check_nesting(path: Path) -> None:
    """Balanced totals can still hide a missing close paired with a stray one, so
    walking the running brace depth catches the case where a function ends but a
    sibling scope does not."""
    code = strip_comments_and_strings(path.read_text(encoding="utf-8"))
    depth = 0
    for lineno, line in enumerate(code.split("\n"), start=1):
        for char in line:
            if char == "{":
                depth += 1
            elif char == "}":
                depth -= 1
                if depth < 0:
                    errors.append(
                        f"{path.relative_to(ROOT)}:{lineno}: closing brace with no open scope"
                    )
                    return
    if depth != 0:
        errors.append(f"{path.relative_to(ROOT)}: {depth} unclosed block(s) at end of file")


COLOUR_RE = re.compile(r'#[0-9A-Fa-f]{9,}\b')


def check_colour_literals(path: Path) -> None:
    """Android colour literals are #RGB, #ARGB, #RRGGBB or #AARRGGBB.

    AAPT rejects anything longer, and the failure only appears in a real build, so
    every resource XML is checked here. This exact bug shipped once already: the
    mark generator emitted #FF96FFFFFF (alpha prefixed twice).
    """
    for match in COLOUR_RE.finditer(path.read_text(encoding="utf-8")):
        errors.append(
            f"{path.relative_to(ROOT)}: '{match.group(0)}' is not a valid colour literal "
            "(use #AARRGGBB, not #FFAARRGGBB)"
        )


def check_balance(path: Path) -> None:
    code = strip_comments_and_strings(path.read_text(encoding="utf-8"))
    for opening, closing in (("{", "}"), ("(", ")"), ("[", "]")):
        if code.count(opening) != code.count(closing):
            errors.append(
                f"{path.relative_to(ROOT)}: unbalanced {opening}{closing} "
                f"({code.count(opening)} open vs {code.count(closing)} close)"
            )


# ---------------------------------------------------------------- declarations
declared_types: dict[str, Path] = {}
top_level_functions: dict[str, list[Path]] = defaultdict(list)

FUN_RE = re.compile(
    r"^((?:(?:internal|private|public|protected|suspend|inline|operator|open|override|abstract|tailrec)\s+)*)"
    r"fun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?(\w+)\s*\(",
    re.M,
)
PROPERTY_RE = re.compile(
    r"^((?:(?:internal|private|public|const)\s+)*)(?:val|var)\s+(\w+)\s*(?::|=)",
    re.M,
)
TYPE_RE = re.compile(
    r"^(?:internal |private |public |protected |open |sealed |data |abstract )*"
    r"(?:class|interface|object|enum class|annotation class)\s+(\w+)",
    re.M,
)


def collect_declarations(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    for name in TYPE_RE.findall(text):
        declared_types.setdefault(name, path)
    # properties such as LocalFaceItColors are importable symbols too
    for _modifiers, name in PROPERTY_RE.findall(text):
        declared_types.setdefault(name, path)
    for modifiers, name in FUN_RE.findall(text):
        if "private" in modifiers or "protected" in modifiers:
            # private members are file-scoped: duplicates across files are legal
            declared_types.setdefault(name, path)
            continue
        top_level_functions[name].append(path)


# ------------------------------------------------------------ import checking
IMPORT_RE = re.compile(r"^import\s+([\w.]+)(?:\s+as\s+(\w+))?$", re.M)


def check_imports(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    body = "\n".join(line for line in text.split("\n") if not line.startswith("import "))
    for module, alias in IMPORT_RE.findall(text):
        symbol = alias or module.rsplit(".", 1)[-1]
        if "." not in module or symbol == "R":
            continue
        package = module.rsplit(".", 1)[0]
        # Only resolution we can do without a compiler: our own packages.
        if not package.startswith("com.zamcan.faceit"):
            continue
        known = symbol in declared_types or symbol in top_level_functions
        if not known:
            errors.append(f"{path.relative_to(ROOT)}: imports missing symbol {module}")


# ----------------------------------------------------------- resource checking
def check_resources() -> None:
    available: set[str] = set()
    for folder in RES.iterdir():
        if not folder.is_dir():
            continue
        for file in folder.iterdir():
            if file.suffix in {".png", ".webp", ".jpg", ".xml"}:
                available.add(f"{folder.name.replace('-', '_')}/{file.stem}")
                available.add(file.stem)

    declared_ids: set[str] = set()
    for layout in (RES / "layout").glob("*.xml"):
        declared_ids.update(re.findall(r'@\+id/(\w+)', layout.read_text(encoding="utf-8")))

    declared_strings, declared_colors = set(), set()
    for values in RES.glob("values*"):
        for file in values.glob("*.xml"):
            tree = ET.parse(file)
            for node in tree.getroot():
                name = node.get("name")
                if node.tag == "string":
                    declared_strings.add(name)
                elif node.tag == "color":
                    declared_colors.add(name)
        # only values*/ files inside folders, skip nested
    for name in declared_strings:
        available.add(name)

    for kt in SRC.rglob("*.kt"):
        for kind, name in re.findall(r"\bR\.(\w+)\.(\w+)", kt.read_text(encoding="utf-8")):
            if kind == "id":
                ok = name in declared_ids
            elif kind == "color":
                ok = name in declared_colors
            elif kind == "string":
                ok = name in declared_strings
            else:
                ok = name in available or f"{kind}/{name}" in available
            if not ok:
                errors.append(f"{kt.relative_to(ROOT)}: R.{kind}.{name} has no matching resource")

    for manifest in [ROOT / "app/src/main/AndroidManifest.xml"]:
        text = manifest.read_text(encoding="utf-8")
        for ref in re.findall(r'@(string|color|xml|drawable|mipmap|style|layout)/(\w+)', text):
            kind, name = ref
            if kind == "style":
                continue
            if f"{kind}/{name}" not in available and name not in available:
                errors.append(f"AndroidManifest.xml references missing @{kind}/{name}")

    # every widget info / input-method xml referenced from manifest must exist
    for name in re.findall(r'@xml/(\w+)', ET.tostring(ET.parse(
            ROOT / "app/src/main/AndroidManifest.xml").getroot()).decode()):
        if not (RES / "xml" / f"{name}.xml").is_file():
            errors.append(f"AndroidManifest.xml references missing @xml/{name}")


# ------------------------------------------------------- duplicate definitions
def check_duplicates() -> None:
    for name, paths in top_level_functions.items():
        unique = {p.relative_to(ROOT) for p in paths}
        if len(unique) > 1:
            joined = ", ".join(str(p) for p in sorted(unique))
            errors.append(f"top-level fun {name}() declared in more than one file: {joined}")


# ------------------------------------------------------------------ policy
def check_policies() -> None:
    """Guard the binding product decisions that are easy to reintroduce."""

    manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    if "BIND_INPUT_METHOD" in manifest or "android.view.InputMethod" in manifest:
        errors.append(
            "AndroidManifest.xml registers an input method; AGENTS.md §10 forbids shipping a "
            "system keyboard without a new product-owner decision"
        )
    if (RES / "xml" / "faceit_input_method.xml").exists():
        errors.append("res/xml/faceit_input_method.xml exists; remove the IME subtype resource")

    for kt in SRC.rglob("*.kt"):
        # comments may legitimately discuss the IME ban; only code counts
        text = strip_comments_and_strings(kt.read_text(encoding="utf-8"))
        if "InputMethodService" in text:
            errors.append(f"{kt.relative_to(ROOT)} references InputMethodService (AGENTS.md §10)")

    # The keyboard gallery must not imply it can change another app's keyboard.
    gallery = SRC / "com/zamcan/faceit/ui/KeyboardDesignGallery.kt"
    if gallery.is_file():
        text = strip_comments_and_strings(gallery.read_text(encoding="utf-8"))
        for banned in ("Settings.ACTION_INPUT_METHOD_SETTINGS", "showInputMethodPicker"):
            if banned in text:
                errors.append(
                    f"KeyboardDesignGallery.kt calls {banned}; the gallery must not send users "
                    "to system keyboard settings (AGENTS.md §10)"
                )

    # No committed keystores or signing secrets (AGENTS.md §20).
    for path in ROOT.rglob("*"):
        if ".git" in path.parts or not path.is_file():
            continue
        if path.suffix.lower() in {".jks", ".keystore"} or path.name == "keystore.properties":
            errors.append(f"{path.relative_to(ROOT)}: signing material must never be committed")



HEX8 = re.compile(r"0x([0-9A-Fa-f]{8})(?![0-9A-Fa-f])(?!\.toInt\()")
COLOUR_API = re.compile(
    r"(intArrayOf|drawColor|setColor|\bcolor\s*=|\bargb\s*=|\btint\s*=|\bstrokeColor\s*=)"
)


def check_long_literals(files, problems):
    """An 8-digit hex literal above 0x7FFFFFFF is a Long in Kotlin.

    Passing one where an Int is required is a compile error, and it is invisible
    while reading, because `0x88000000` looks exactly like a colour. A shader
    colour literal has already reached CI as a Long once, which is what this guard
    is for. Compose's `Color(0x...)` is exempt: that overload takes a Long and is
    correct as written.
    """
    for path in files:
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if line.lstrip().startswith(("*", "//", "/*")):
                continue
            if not COLOUR_API.search(line):
                continue
            # Color(0x...) / Color(0x...) in Compose takes a Long on purpose.
            if "Color(0x" in line and "intArrayOf" not in line:
                continue
            for digits in HEX8.findall(line):
                if int(digits, 16) > 0x7FFFFFFF:
                    problems.append(
                        f"{path.name}:{number}: 0x{digits} is a Long where an Int is "
                        f"required - add .toInt()"
                    )


def main() -> int:
    kotlin = sorted(SRC.rglob("*.kt"))
    if not kotlin:
        print("no Kotlin sources found")
        return 1
    for resource in sorted(RES.rglob("*.xml")):
        check_colour_literals(resource)
    for path in kotlin:
        check_balance(path)
        check_nesting(path)
        collect_declarations(path)
    for path in kotlin:
        check_imports(path)
    check_duplicates()
    check_resources()
    check_policies()
    check_long_literals(kotlin, errors)

    print(f"Face It source verification: {len(kotlin)} Kotlin files, {len(declared_types)} declared types")
    # Split so a file added but never picked up is visible rather than silently
    # absorbed into a total that also counts something else.
    test_sources = sorted(ROOT.glob("app/src/test/**/*.kt"))
    print(f"  main sources: {len(kotlin)} · test sources: {len(test_sources)}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    print("PASS: delimiters balanced, project-internal imports resolve, resources referenced exist.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
