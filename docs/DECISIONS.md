# Architectural Decision Records & Trade-off Analysis

This document details our non-obvious architectural decisions, assumptions, and design choices made during the development of the Smart Lights Control demo app.

---

## 1. Light Selection via Dropdown (vs. Tappable House Image Hotspots)
- **Context**: The brief requires managing at least three lights, but the Figma home screen design shows a single light widget.
- **Decision**: A compact dropdown selector row sits between the house image and the widget grid to switch between the seed lights (`"Bed light"`, `"All the lights in the house"`, `"Entrance light"`).
- **Alternatives Considered**: Implementing custom coordinate hit-testing and tap hotspots on the house illustration.
- **Why / Trade-off Rationale**: Coordinate hit-testing is brittle across varying screen dimensions and difficult to make fully accessible. A dropdown guarantees robust accessibility semantics, precise touch targets (48 dp), and straightforward multi-light management.

---

## 2. Group Light Behavior ("All the lights in the house")
- **Context**: The seed data includes a group light (`"All the lights in the house"`), but the brief does not specify group toggle semantics.
- **Decision**: Toggling the group turns every light on or off collectively without setting a single brightness percentage. Turning the group off and back on restores each individual light's previous `lastBrightness`.
- **Alternatives Considered**: Treating the group as a dimmable light that overrides all individual brightness levels.
- **Why / Trade-off Rationale**: Grouping lights in a smart home typically controls power state across zones. Preserving individual `lastBrightness` values provides a more intuitive and non-destructive user experience when toggling groups.

---

## 3. Reaching 0% Brightness Turns the Light Off
- **Context**: The brief specifies brightness values from 0% to 100%, but does not state whether 0% keeps power ON or turns it OFF.
- **Decision**: Dragging a slider or swiping down to 0% automatically turns the light off (`isOn = false`). A light is never considered ON at 0%.
- **Alternatives Considered**: Keeping the light ON at 0% brightness.
- **Why / Trade-off Rationale**: In lighting control UX, 0% brightness is functionally identical to powered off. Turning the light off at 0% simplifies the state machine and matches user mental models.

---

## 4. `lastBrightness` Persistence Logic
- **Context**: The `Light` model includes a `lastBrightness` field.
- **Decision**: When a light turns off or drops to 0%, its last non-zero brightness is stored in `lastBrightness`. Toggling the light back on restores that brightness (defaulting to 75% if none was recorded).
- **Alternatives Considered**: Always turning lights back on to a fixed default brightness (e.g., 100%).
- **Why / Trade-off Rationale**: Preserving user preference (`lastBrightness`) prevents frustration where turning a light back on blinds the user with full brightness.

---

## 5. Drawer Header Status Title ("On" / "Off")
- **Context**: Figma shows the drawer header with light name and a status title.
- **Decision**: The drawer header displays the light name as a subtitle and the current state ("On" or "Off") as the status title.
- **Alternatives Considered**: Hardcoding a static title like "Device status".
- **Why / Trade-off Rationale**: Showing the live state ("On" / "Off") prominently in the drawer header provides immediate, unambiguous feedback to the user.

---

## 6. Error Toast Dismissal & Auto-Dismiss Behavior
- **Context**: The brief requires showing *"Something went wrong · Try again later"* on failure, but does not specify dismiss mechanics.
- **Decision**: The error toast features:
  1. Manual close (×) button (`HomeScreenEvent.DismissError`).
  2. Auto-dismiss after 4 seconds.
  3. Immediate dismissal on any subsequent user interaction (tap, swipe, selection change) to prevent stale errors from lingering over successful actions.
- **Alternatives Considered**: Persistent banners requiring manual dismissal.
- **Why / Trade-off Rationale**: Persistent banners block the UI and cause user frustration if a subsequent action succeeds. Auto-dismissal and action-triggered clearing ensure error feedback is timely without being intrusive.

---

## 7. Optimistic Update Rollback to Last Confirmed State (`confirmedLights`)
- **Context**: The brief requires an optimistic UI that rolls back if the service call fails.
- **Decision**: The repository applies changes immediately to the `StateFlow`, executes the service call, and on failure rolls back to the last **confirmed state** (`confirmedLights[id]`), rather than the state immediately before the failed action.
- **Alternatives Considered**: Reverting to the exact state snapshot before the failed request.
- **Why / Trade-off Rationale**: Reverting to a naive snapshot risks clobbering concurrent, successful actions performed on *other* lights while a slow request was in flight. Confirmed-state rollback guarantees absolute multi-light concurrency safety.

---

## 8. No UseCase Layer (Direct ViewModel-to-Repository)
- **Context**: The brief leaves architectural layering entirely up to the developer.
- **Decision**: The ViewModel talks directly to `LightsRepository` without an intermediate UseCase layer.
- **Alternatives Considered**: Creating explicit UseCase classes for every operation.
- **Why / Trade-off Rationale**: Avoids speculative indirection and boilerplate in a single-screen app, while keeping the repository cleanly abstracted behind an interface so UseCases can be introduced instantly if the domain expands.

---

## 9. Horizon Pattern for State & Event Handling (vs. Classic MVI)
- **Context**: The brief leaves state management choices up to the developer.
- **Decision**: Adopt the Compose-native Horizon Pattern, where `HomeScreenState` encapsulates all UI state and exposes an `eventTunnel: (HomeScreenEvent) -> Unit`, eliminating event/effect channels entirely.
- **Alternatives Considered**: Classic MVI with Effect channels (`Channel` / `SharedFlow`).
- **Why / Trade-off Rationale**: Classic MVI suffers from side-effect event loss across configuration changes and high boilerplate. Horizon unifies state emission and event reception into a single immutable contract, making state persistence and UI testing trivial.

---

## 10. Customizable Failure Rate (0% to 100%) via Header Cycle Pill
- **Context**: The brief requires a configurable failure rate (default ~10%) to test error flows.
- **Decision**: Provide an interactive **"Fail: X%"** cycle pill in the top header (`HomeHeader`) that cycles failure probabilities (`0% ➔ 10% ➔ 50% ➔ 100% ➔ 0%`) live.
- **Alternatives Considered**: Hardcoding failure rates in code or configuration files.
- **Why / Trade-off Rationale**: Makes manual QA and live demoing of error rollback and toast behaviors instantly reproducible during live review calls without restarting the app or editing source files.

---

## 11. Design Tokens Extracted from Screenshots
- **Context**: The Figma source file was inaccessible; design references were provided as screenshots.
- **Decision**: Colors were sampled directly from the provided design screenshots, while sizes, spacing, and typography were estimated and scaled.
- **Alternatives Considered**: Guessing an arbitrary color palette.
- **Why / Trade-off Rationale**: Sampling from screenshots guarantees high visual fidelity to the intended design despite missing Figma design tokens.

---

## 12. System Default Font (Roboto)
- **Context**: Typography font family from Figma was unspecified.
- **Decision**: The system default sans-serif font (Roboto) is used without bundling custom font files.
- **Alternatives Considered**: Bundling custom font `.ttf` files.
- **Why / Trade-off Rationale**: Avoids unnecessary asset bloat and ensures native platform typography rendering across different device densities.

---

## 13. App Launcher Icon Set to `bulb_on`
- **Context**: Establishing branded launcher icons (`ic_launcher` and `ic_launcher_round`).
- **Decision**: The app launcher icon utilizes `bulb_on` as the foreground drawable for the adaptive icon configuration.
- **Alternatives Considered**: Using default Android studio template icons.
- **Why / Trade-off Rationale**: Aligns launcher icon branding directly with the active smart bulb theme used across the application.
