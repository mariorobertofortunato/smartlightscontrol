# Context 
I'm building a small Android demo app for a technical interview. 
Single screen: a Home screen with a "Manage Lights" widget + a Light Drawer (bottom sheet). 
Everything else is a static placeholder. 
Stack: Kotlin, Jetpack Compose, Koin, coroutines/Flow. 
Requirements are in docs/requirements.pdf, design in Figma (file "Lights — SmartHomeApp", page "Flows", 5 flows)
https://www.figma.com/design/kGgxyEJMMF4j0rNhwyQzUL/Lights----SmartHomeApp?node-id=1-3 
IMPORTANT: if you can't access the Figma let me know, DO NOT assume or come up with random UI until I provide clarification or some other form of mock up. 

What reviewers evaluate: fidelity to the UX flow, state management, code quality, and trade-off reasoning. 
So: simple, readable, well-tested code over clever code.

# Working rules 
- Work in the steps below. After EACH step: stop, summarize what you did, list assumptions and open questions, and wait for my approval before continuing. 
- Before writing code in a step, state your assumptions. If a requirement or Figma screen is ambiguous, ask — don't pick silently. 
- Minimum code that solves the requirement. No speculative abstractions, no configurability that isn't required, no UseCases (ViewModel talks to Repository directly; we will document this trade-off). 
- Each step must compile and pass tests before moving on. 
- Keep a running docs/DECISIONS.md with every non-obvious trade-off (one short entry per decision). 
- Images are provided in the project assets; use them as static images.

# Architecture 
- domain: `Light` model, `LightsRepository` interface. No Android/data dependencies. 
- data: `LightService` interface + `MockLightService`, `LightsRepositoryImpl`. 
- presentation: `HomeScreen`, `HomeViewModel`, composables, theme. 
- DI: Koin module(s) binding service, repository, ViewModel. 
- Single source of truth: `LightsRepository` exposes `StateFlow<List<Light>>`. Widget and drawer BOTH read from it (via the ViewModel). No duplicated light state in UI. 
- Async: `suspend` functions (the spec's Promise = suspend fun). No Deferred unless justified. 
Light model: id, name, dimmable, isOn, brightness (0–100, ignored if not dimmable), lastBrightness. Seed: "Bed light" (dimmable, on, 75%), "All the lights in the house" (group, dimmable, on, 100%), one non-dimmable light.

# Presentation pattern: Horizon Pattern — mandatory
The presentation layer MUST follow the Horizon Pattern (Compose-native evolution of MVI; reference: https://medium.com/@mariorobertofortunato/the-horizon-pattern-an-evolution-beyond-classic-mvi-in-kotlin-apps-with-jetpack-compose-43be5281713a). Do not substitute classic MVI (no Intent/Effect/Reducer layers, no Channel, no SharedFlow).

Rules:
- Contract.kt per screen: a sealed `HomeScreenEvent` and an immutable `HomeScreenState` data class. Events represent intentions (e.g. WidgetTapped, BrightnessChanged, OpenDrawer), not UI callbacks.
- `HomeScreenState` holds everything the screen renders (lights already adapted for display, drawer open/selected light, error fields) AND `val eventTunnel: (HomeScreenEvent) -> Unit = {}`.
- `HomeScreenViewModel` exposes only `state: StateFlow<HomeScreenState>` (private MutableStateFlow + asStateFlow). It builds the initial state with `eventTunnel = { processEvent(it) }`. A single private `processEvent(event)` with an exhaustive `when` (no `else -> {}`) is the only entry point. All logic and side effects live here; state changes via `_state.update { it.copy(...) }`.
- The state is derived from the repository's `StateFlow<List<Light>>` (collect it in the ViewModel and copy into state). No duplicated light state.
- NO one-shot effects. Errors are state: e.g. `widgetError: Boolean` / `drawerError: Boolean` (or a small sealed type), set by the ViewModel when a repository call fails, and cleared by an explicit event (e.g. `DismissError`) or an auto-dismiss done in the ViewModel. Error origin (widget vs drawer) is known by the ViewModel from the event that triggered the call.
- UI is dumb: `HomeScreen` collects state with `collectAsState()` (or `collectAsStateWithLifecycle`), provides the tunnel via `CompositionLocalProvider(LocalHomeScreenEventTunnel provides state.eventTunnel)`, and children call `LocalHomeScreenEventTunnel.current(event)` instead of prop drilling. `staticCompositionLocalOf` with an `error("not provided")` default. No business logic in composables.
- Previews/tests: provide a fake tunnel via the CompositionLocal and hardcoded State instances.
- DI is Koin (`koinViewModel()`), not Hilt. No UseCases: the ViewModel talks to `LightsRepository` directly (documented trade-off).

# Steps 
- Step 0 — Understand (no code) Read the PDF and inspect all 5 Figma flows. Produce: 
	1. A table of the 11 acceptance criteria mapped to screens/states in Figma. 
	2. Extracted design tokens (colors, typography, spacing, radii) → docs/design-tokens.md. 
	3. A list of ambiguities. In particular: (a) how multiple lights are shown/selected on Home, (b) what "Widget placement: if the latest action was turning on, the widget offers the opposite action" means concretely in the Figma, (c) what exactly the error UI looks like in widget vs drawer. 
	Do not write code. Wait for my answers. 
- Step 1 — Project skeleton Packages, Koin setup, theme (dark, tokens from Step 0), `Light` model, interfaces, empty HomeScreen showing seed data as plain text. Verify: app builds and runs, Koin graph resolves. 
- Step 2 
	- Mock service + repository + unit tests 
	- `MockLightService`: in-memory lights, delay 300–800 ms, configurable failure rate (default 0.1), injectable `Random`/delay so tests are deterministic. 
	- `LightsRepositoryImpl`: holds `StateFlow<List<Light>>`, keeps per-light last CONFIRMED state. 
	- Optimistic logic: apply change to state immediately → call service → on success store confirmed state; on failure roll back to the last confirmed state (NOT "state before this action", to stay correct with concurrent actions) and emit an error. 
	- Toggle on restores `lastBrightness`; toggle off keeps it. Non-dimmable: toggle only. 
	- Brightness: UI-facing value updates immediately, service call is debounced/throttled (send final value only). 
	- Repository operations are suspend functions returning Result (or throwing). On failure the repository has already rolled back to the last confirmed state and reports the failure to the caller. No error stream, no SharedFlow/Channel. 
	Tests first (JUnit + kotlinx-coroutines-test + Turbine if useful): toggle on/off, lastBrightness restore, brightness change, debounce sends one request, rollback on failure, rollback with two overlapping actions, non-dimmable behavior. Verify: all tests green. 
- Step 3 — Define Contract.kt (HomeScreenEvent, HomeScreenState with eventTunnel) and HomeScreenViewModel per the Horizon Pattern. Unit-test via state.value.eventTunnel(event) / state transitions, including error fields set on failure (widget vs drawer origin) and cleared by DismissError. Verify: tests green. 
- Step 4 — Widget (dimmable + non-dimmable) Compose widget per Figma: label "On · 75%" / "Off · 0%", live fill on swipe, tap toggles, arrow opens drawer. Non-dimmable: "On"/"Off" only, tap or swipe toggles. Static placeholders for blinds/scenes/assistant card/house image. Accessibility: 44×44 touch targets, content descriptions / semantics (incl. custom actions or setProgress for brightness). Verify: @Preview for each state; manual check against Figma. 
  Composables are stateless and send events only through LocalHomeScreenEventTunnel. Previews use hardcoded State + a no-op tunnel.
- Step 5 — Drawer ModalBottomSheet per Figma: Turn on/off button, status text, bulb visual (min/max image states), brightness slider 0–100 (only for dimmable), "This is a non-dimmable light" text for non-dimmable. Closing the drawer must leave the widget showing the latest state (guaranteed by the single source of truth). Verify: previews + manual flow. 
  Composables are stateless and send events only through LocalHomeScreenEventTunnel. Previews use hardcoded State + a no-op tunnel.
- Step 6 — Error flows Widget: 
  Errors are state fields (widgetError/drawerError) rendered by the UI, not effects.
  On failure, UI rolls back and shows "Something went wrong · Try again later". Drawer: same style on action failure, and on failure to open (decide with me how to simulate it; propose options). Make failure rate easy to force to 100% for demo (e.g. a debug flag), documented in README. 
- Step 7 — Tests + polish At least one Compose UI interaction test (tap widget → label changes; failure → rollback + error shown). Review accessibility. Compare each screen with Figma and list remaining visual deltas (don't fix unrequested things). 
  The Compose interaction test provides a fake/real tunnel via the CompositionLocal.
- Step 8 — README Architecture, how to run, how to trigger errors, trade-offs (from DECISIONS.md: no UseCases, rollback-to-confirmed strategy, debounce choice, state ownership), what I'd do with more time. Why Horizon over classic MVI: single event entry point, no effect channels, errors modeled as state.

Start with Step 0 only.