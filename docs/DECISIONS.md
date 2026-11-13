# Decisions

One short entry per non-obvious trade-off.

## 1. Light selection: dropdown, not tappable house image

The design shows a single light widget but the brief requires at least three lights.
A dropdown selects which light the widget controls. It sits in a compact row between the
house image and the widget grid; this row is not in the design.

Tappable hotspots on the house image were rejected: more hit-testing and accessibility
work for something the brief allows to be a static image. Highlighting the selected light
on the house image is deferred (listed under "what I'd do next").

## 2. Group light only switches power

"All the lights in the house" turns every light on or off. It does not set brightness.
Turning the group off and on again restores each light's previous brightness (a light at
75% goes to 0% and comes back at 75%).

In the UI the group is treated as a non-dimmable light: "On" / "Off" with no percentage,
no slider, and the "This is a non-dimmable light" text in the drawer.

This is an assumption; the brief does not define group behaviour.

## 3. Reaching 0% turns the light off

- Dimmable widget: swiping right on a light that is off turns it on; swiping left down to 0
  turns it off.
- Drawer slider: dragging to 0 turns the light off too. A light at 0% is always OFF.
- Non-dimmable widget: directional toggle (right = on, left = off).

## 4. `lastBrightness` stores the last non-zero brightness

When a light turns off (or drops to 0%), its last non-zero brightness is stored as `lastBrightness`.
When turned back on, it restores that `lastBrightness` (defaulting to 75% if none was recorded).
A light is never ON at 0%.

## 5. Drawer status title shows current state

The drawer header displays the current state ("On" / "Off") as the status title, as agreed.

## 6. Error toast dismiss behaviour

The error toast features a manual close (×) button and also auto-dismisses after 4 seconds.
Errors are state fields in `HomeScreenState` as required by the Horizon pattern.

## 5. Touch targets are 48 dp

The brief asks for 44×44 pt (the iOS figure). The Android guideline of 48 dp is used; it
satisfies both.

## 6. Design tokens come from screenshots

The Figma file could not be opened. Colours were sampled from the two design screenshots
in the project; sizes, spacing, radii and typography are estimates. The error toast and
non-dimmable states come from screenshots that were only viewed, so their colours are
estimated by eye. See `design-tokens.md`.

## 7. Font is the system default

The font family used in the design is unknown. The system default sans-serif (Roboto on
most devices) is used, so no font files are bundled.
