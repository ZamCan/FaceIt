# FaceIt Clock Widget

The first native home-screen widget is a configurable time and date surface hosted by Android.

## User flow

1. Open the Android launcher widget picker.
2. Add the FaceIt Clock widget.
3. Android opens FaceIt's widget configuration activity.
4. Choose a visual finish and confirm Add widget.
5. Tap the widget to open FaceIt.

## Available finishes

- **Glazed:** Obsidian Glass, Midnight Silk, Noir, Pearl Mint.
- **Ceramic:** Warm Ceramic, Warm Paper, Pearl Shell.
- **Satin:** Rose Satin, Sage Satin.

The original persisted IDs — `Obsidian`, `Ceramic` and `Mint` — remain unchanged so already-placed widgets keep their finish after an update. Each widget instance stores its own selected style. Both time and date are rendered by Android TextClock views, so the widget stays current without a FaceIt timer, wake lock or periodic app-side refresh.

## Resource and platform rules

- Android hosts and schedules the widget.
- No permanent service, wake lock or continuous 3D renderer.
- Widget taps open the FaceIt application.
- Each widget ID has independent style preferences.
- Launcher placement and resizing remain controlled by the Android launcher.

This is the first real widget integration, not the complete future Widget Studio. The Style Studio may preview the three installed clock finishes, but it does not silently change existing widget instances. Additional calendar, photo, battery and shortcut widgets require their own tested native providers and configuration flows.
