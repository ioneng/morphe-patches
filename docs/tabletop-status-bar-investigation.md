# Tabletop status bar investigation

Target APK: YouTube 21.30.216 (1561187872). The APK itself is not included.

## Static bytecode observations

- classes4.dex: `Lgsl;` initializes FLAT and HALF_OPENED state objects.
- classes2.dex: `Lafmv;->a(Ljava/lang/Object;)Ljava/lang/Object;` includes a mapping of a single half-open folding feature: zero-height bounds produce a horizontal clamshell state using Rect.top; zero-width bounds produce a book state using Rect.left. Other inputs fall back to other posture states.
- classes5.dex: `Lafrn;`, `Lafrm;`, and `Lafro;` implement `Lafrk;`. Their toString methods contain `ClamshellState{foldPositionY=`, `BookState{foldPositionX=`, and `FlatState{}` respectively.
- classes.dex: `Losl;->q(Lafrk;)V` and `Lpah;->q(Lafrk;)V` both save the result of an instanceof ClamshellState check to a boolean field, compare it to the prior value, and call an update method on change.
- classes.dex: `Lrgh;->a` also checks ClamshellState in a path that updates `Lrhb;`. That class contains the string `tabletop-controls`, but its observed callers appear to build event payloads, so it is not the preferred UI hook.

## Probe

The existing Hide status bar patch now includes a diagnostic injection which resolves the clamshell class by its string and matches the two callback shapes. The injection logs the boolean result and the existing PlayerType. It does not change status bar visibility or add a setting.

Test on the N6 with Morphe debug logging enabled, the global hide status bar setting disabled, and a regular video active. Enter tabletop, leave tabletop, and repeat using Fold Switcher. Also check the home feed, miniplayer and normal fullscreen. Look for `Tabletop probe:` logs and record player states. Confirm callbacks track the UI rather than just the device posture.

## Next implementation

Keep the existing global setting and its saved value compatible. Add a tabletop-only mode after verifying the probe. Hide only the main player's status bar, restore it on leaving the tabletop player, and preserve stock fullscreen and navigation-bar handling. The existing status bar layout fix caches edge-to-edge flags for the session; do not switch those feature flags dynamically. Validate inset and display cutout handling separately.

This is an uncompiled diagnostic prototype; it has not been run against a patched APK or tested on a device. Fingerprints need validation across supported targets before submitting upstream.
