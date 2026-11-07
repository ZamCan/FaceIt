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



def tap_label(label):
    tree = wait_for(lambda tree: bool(nodes(tree, label)), "Missing control: " + label)
    bounds = list(map(int, re.findall(r"\d+", nodes(tree, label)[-1].get("bounds", ""))))
    x1, y1, x2, y2 = bounds
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))


def scene_round_trip():
    tap_label("Create")
    tap_label("Save")
    wait_for(lambda tree: any("saved to your scene library" in n.get("text", "")
                             for n in tree.iter("node")), "Scene was not saved")
    # Verify the collection survives process death, not just composition state.
    adb("shell", "am", "force-stop", PACKAGE)
    adb("shell", "am", "start", "-W", "-n", PACKAGE + "/com.zamcan.faceit.MainActivity")
    tap_label("Library")
    tap_label("Saved scenes")
    wait_for(lambda tree: len(nodes(tree, "Restore scene")) == 1, "Saved scene did not survive restart")
    tap_label("Duplicate")
    wait_for(lambda tree: len(nodes(tree, "Restore scene")) == 2, "Duplicate was not added")
    capture("saved-scenes")
    tap_label("Restore scene")
    wait_for(lambda tree: selected(tree, "Discover"), "Restore did not return to the composition")
    print("PASS: scene save, process restart, duplicate and restore", flush=True)



def tap_description(description):
    tree = wait_for(lambda tree: any(n.get("content-desc") == description for n in tree.iter("node")),
                    "Missing action: " + description)
    node = next(n for n in tree.iter("node") if n.get("content-desc") == description)
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))


def scroll_to_label(label, upward=False):
    dimensions = list(map(int, re.findall(r"\d+", adb("shell", "wm", "size").decode())))[-2:]
    width, height = dimensions
    for _ in range(6):
        if nodes(hierarchy(), label):
            return
        start, end = ((.3, .8) if upward else (.8, .3))
        adb("shell", "input", "swipe", str(width // 2), str(int(height * start)),
            str(width // 2), str(int(height * end)), "350")
    raise AssertionError("Control not reachable by scrolling: " + label)


def open_gallery():
    scroll_to_label("Photograph library")
    tap_label("Photograph library")
    wait_for(lambda tree: bool(nodes(tree, "Search wallpapers")), "Wallpaper search did not open")



def wallpaper_ids():
    dump = adb("shell", "dumpsys", "wallpaper").decode()
    (OUTPUT / "wallpaper-state.txt").write_text(dump)
    ids = set(re.findall(r"(?:mWallpaperId|\bid)\s*=\s*(\d+)", dump))
    assert ids, "Android did not report wallpaper identifiers; inspect wallpaper-state.txt"
    return ids


def wait_for_wallpaper_change(previous):
    deadline = time.monotonic() + 60
    while time.monotonic() < deadline:
        current = wallpaper_ids()
        if current - previous:
            print("Wallpaper identifiers changed from " + str(sorted(previous)) + " to " + str(sorted(current)), flush=True)
            return
        time.sleep(1)
    raise AssertionError("Android wallpaper identifiers did not change after Apply")


def catalogue_round_trip():
    open_gallery()
    tap_label("Search wallpapers")
    adb("shell", "input", "text", "zzzz_no_matches_777")
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    wait_for(lambda tree: any("No wallpapers match" in n.get("text", "") for n in tree.iter("node")),
             "Search did not filter the collection")
    adb("shell", "am", "force-stop", PACKAGE)
    adb("shell", "am", "start", "-W", "-n", PACKAGE + "/com.zamcan.faceit.MainActivity")
    open_gallery()
    tap_description("Add favourite")
    tap_label("Favourites")
    wait_for(lambda tree: any(n.get("content-desc") == "Remove favourite" for n in tree.iter("node")),
             "Favourite was not selected")
    capture("gallery-favourites")
    adb("shell", "am", "force-stop", PACKAGE)
    adb("shell", "am", "start", "-W", "-n", PACKAGE + "/com.zamcan.faceit.MainActivity")
    open_gallery()
    tap_label("Favourites")
    wait_for(lambda tree: any(n.get("content-desc") == "Remove favourite" for n in tree.iter("node")),
             "Favourite did not survive process restart")
    tap_description("Remove favourite")
    wait_for(lambda tree: any("No wallpapers match" in n.get("text", "") for n in tree.iter("node")),
             "Favourite was not removed")
    tap_description("Back")
    scroll_to_label("Apply", upward=True)
    previous_wallpapers = wallpaper_ids()
    tap_label("Apply")
    tap_label("Home screen")
    wait_for_wallpaper_change(previous_wallpapers)
    capture("wallpaper-applied")
    print("PASS: offline search, persistent favourite, removal and Android wallpaper application", flush=True)


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
            if mode == "light":
                scene_round_trip()
                catalogue_round_trip()
    finally:
        if font == "null":
            adb("shell", "settings", "delete", "system", "font_scale")
        else:
            adb("shell", "settings", "put", "system", "font_scale", font)
        adb("shell", "cmd", "uimode", "night", night if night in ("yes", "no", "auto") else "auto")


if __name__ == "__main__":
    try:
        main()
    except BaseException:
        OUTPUT.mkdir(parents=True, exist_ok=True)
        try:
            capture("failure")
            (OUTPUT / "failure-logcat.txt").write_bytes(adb("logcat", "-d", "-t", "1500"))
        except Exception:
            pass
        raise


