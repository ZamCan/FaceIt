#!/usr/bin/env python3
"""Exercise real Android navigation and capture built-app screenshots via adb.

Run against an installed debug APK on a disposable emulator. This changes its
night mode/font scale and restores the original settings in finally.
"""
import re
import subprocess
import time
from pathlib import Path
from xml.etree import ElementTree as ET

OUTPUT = Path("work/ui-smoke")
PACKAGE = "com.zamcan.faceit.debug"
TABS = ("Discover", "Themes", "Create", "Library", "Tools")


def adb(*args):
    return subprocess.check_output(["adb", *args], timeout=30)


def hierarchy():
    adb("shell", "uiautomator", "dump", "/sdcard/faceit-ui.xml")
    return ET.fromstring(adb("shell", "cat", "/sdcard/faceit-ui.xml"))


def wait_for(predicate, message):
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline:
        try:
            tree = hierarchy()
            if predicate(tree):
                return tree
        except (subprocess.CalledProcessError, ET.ParseError):
            pass
        time.sleep(1)
    raise AssertionError(message)


def nodes(tree, label):
    return [n for n in tree.iter("node") if n.get("text") == label]


def selected(tree, label):
    # Compose may merge the label into its selectable parent.
    def contains(node):
        return any(n.get("text") == label for n in node.iter("node"))
    return any(n.get("selected") == "true" and contains(n) for n in tree.iter("node"))


def capture(name):
    (OUTPUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p"))
    (OUTPUT / (name + ".xml")).write_bytes(ET.tostring(hierarchy()))


def visit_tabs(mode):
    adb("shell", "am", "force-stop", PACKAGE)
    adb("shell", "am", "start", "-W", "-n", PACKAGE + "/com.zamcan.faceit.MainActivity")
    wait_for(lambda tree: bool(nodes(tree, "Discover")), "App navigation did not appear")
    for label in TABS:
        tree = hierarchy()
        candidates = nodes(tree, label)
        assert candidates, "Missing navigation tab: " + label
        bounds = list(map(int, re.findall(r"\d+", candidates[-1].get("bounds", ""))))
        assert len(bounds) == 4, "Invalid navigation bounds"
        x1, y1, x2, y2 = bounds
        adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
        wait_for(lambda tree: selected(tree, label), "Destination reset or failed: " + label)
        # Capture after asynchronous artwork and the short destination transition settle.
        time.sleep(2)
        assert selected(hierarchy(), label), "Destination did not remain selected: " + label
        capture(mode + "-" + label.lower())
    print("PASS: all five destinations remain selected in " + mode, flush=True)


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    font = adb("shell", "settings", "get", "system", "font_scale").decode().strip()
    night = adb("shell", "cmd", "uimode", "night").decode().strip().split()[-1]
    try:
        for mode, night_mode, scale in (
            ("light", "no", "1.0"),
            ("dark", "yes", "1.0"),
            ("large-type", "yes", "1.5")):
            adb("shell", "cmd", "uimode", "night", night_mode)
            adb("shell", "settings", "put", "system", "font_scale", scale)
            visit_tabs(mode)
    finally:
        if font == "null":
            adb("shell", "settings", "delete", "system", "font_scale")
        else:
            adb("shell", "settings", "put", "system", "font_scale", font)
        adb("shell", "cmd", "uimode", "night", night if night in ("yes", "no", "auto") else "auto")


if __name__ == "__main__":
    main()
