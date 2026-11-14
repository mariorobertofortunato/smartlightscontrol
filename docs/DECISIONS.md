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

When a light turns off (or drops to 0%), its last non-zero brightness is stored as
`lastBrightness`. When turned back on, it restores that `lastBrightness` (defaulting to
75% if none was recorded). A light is never ON at 0%.

## 5. Drawer status title shows current state

The drawer header displays the current state ("On" / "Off") as the status title, as agreed.

## 6. Error toast dismiss behaviour

The error toast features:
- Manual close (×) button sending `HomeScreenEvent.DismissError`.
- Auto-dismiss after 4 seconds if untouched.
- Immediate dismissal on any subsequent user interaction (tap, swipe, light selection) to avoid displaying a stale error over a successful new action.
Errors are state fields (`widgetError`, `drawerError`) in `HomeScreenState` as required by the Horizon pattern.

## 7. Touch targets are 48 dp

The brief asks for 44×44 pt (the iOS figure). The Android guideline of 48 dp is used; it
satisfies both.

## 8. Design tokens come from screenshots

The Figma file could not be opened. Colours were sampled from the two design screenshots
in the project; sizes, spacing, radii and typography are estimates. The error toast and
non-dimmable states come from screenshots that were only viewed, so their colours are
estimated by eye. See `design-tokens.md`.

## 9. Font is the system default

The font family used in the design is unknown. The system default sans-serif (Roboto on
most devices) is used, so no font files are bundled.

## 10. Optimistic update with rollback to last CONFIRMED state

The repository applies every change to the `StateFlow` immediately (optimistic UI), calls
the service, and only on success stores the response as the new confirmed state. On failure
it rolls back to the last confirmed state — not to "the state before this action" — so
concurrent actions on other lights are never clobbered by a failed call.

## 11. Brightness debounce: 300 ms, final value only

Slider and swipe changes update the UI state instantly, but the service call is debounced
with a 300 ms window: each new value cancels the pending call, so only the final value of
a burst is sent. 300 ms matches the lower bound of the simulated network delay, so a
settled value is never sent while a previous request is still in flight.

## 12. Repository errors are returned, not streamed

Repository operations are suspend functions returning `Result`. There is no error
`SharedFlow`, no `Channel` and no error stream: the caller (the ViewModel) already knows
which action failed and where it came from, so the error origin is known from the event.

## 13. No UseCases: ViewModel talks to the repository directly

The brief forbids speculative abstractions. With a single screen and one repository, a
UseCase layer would add indirection without value. The ViewModel calls
`LightsRepository` directly. If the app grows (multiple screens, transformations,
composition of calls), UseCases can be introduced later without touching the UI.

## 14. Horizon Pattern for state & event handling

The presentation layer strictly follows the Horizon pattern (Compose-native MVI evolution).
`HomeScreenContract.kt` defines `HomeScreenState` and `HomeScreenEvent`. `HomeScreenState` encapsulates
all state and exposes `eventTunnel = { processEvent(it) }`. Composables access the event tunnel via
`LocalHomeScreenEventTunnel` (CompositionLocal) without prop drilling. Errors (widget error vs drawer error)
are modeled directly as state flags in `HomeScreenState` (no one-shot effect channels).

## 15. Customizable failure rate (0% to 100%) with header cycle pill

To facilitate testing and live demonstration of error states and rollback handling, `MockLightService` and `LightsRepository` support a configurable failure rate ranging from 0.0 (0%) to 1.0 (100%).
An interactive **"Fail: X%"** pill in the top header (`HomeHeader`) cycles through preset rates (`0% ➔ 10% ➔ 50% ➔ 100% ➔ 0%`) on tap, providing immediate visual feedback and allowing reviewers to test error recovery and toast behaviors across different failure probabilities.