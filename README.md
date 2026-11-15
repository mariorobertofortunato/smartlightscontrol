# Smart Lights Control Demo App

A production-grade Android demo application built for a technical interview, showcasing modern Jetpack Compose architecture, Koin dependency injection, coroutines/Flow, and the **Horizon Pattern** (Compose-native evolution of MVI).

---

## Architecture & Layers

The project follows a clean, decoupled architecture:
- **`domain`**: Pure Kotlin models (`Light`) and repository interface (`LightsRepository`). Free of Android framework and data dependencies.
- **`data`**: `LightService` interface, `MockLightService` (with in-memory state and configurable failure rate from 0% to 100%), and `LightsRepositoryImpl`.
- **`presentation`**: `HomeScreen`, `LightDrawer` bottom sheet, composable UI components, theme tokens, and `HomeScreenViewModel` adhering strictly to the Horizon Pattern.
- **`di`**: Koin modules binding services, repositories, and ViewModels.

---

## Why Horizon Pattern over Classic MVI?

The presentation layer strictly implements the **Horizon Pattern** (Compose-native MVI evolution), replacing boilerplate-heavy classic MVI for several key architectural reasons:

1. **Elimination of Event-Loss & One-Shot Effect Channels**:
   - In classic MVI, transient UI events (such as showing error toasts or triggering navigation) are typically dispatched using Kotlin `Channel` or `SharedFlow` for one-shot effects. These channels frequently cause bugs: if emitted while a Composable is in the background or during configuration changes/recompositions, the effect is silently dropped or re-delivered incorrectly.
   - **Horizon Solution**: Horizon models errors and transient UI states **directly as immutable state properties** (`widgetError`, `drawerError`, `errorMessage`) inside `HomeScreenState`. Because state is backed by a lifecycle-aware `StateFlow`, error messages and feedback survive configuration changes and process death naturally without any risk of lost events.

2. **Elimination of MVI Boilerplate**:
   - Classic MVI requires maintaining separate classes/interfaces for `Intents`, `States`, `Effects` (or SideEffects), Reducer functions, and complex event loops.
   - **Horizon Solution**: Horizon streamlines this into a clean contract (`HomeScreenContract.kt`) containing an immutable `HomeScreenState` and a sealed `HomeScreenEvent`. The state itself encapsulates the event dispatcher (`eventTunnel: (HomeScreenEvent) -> Unit`), unifying state emission and event reception into a single reactive contract.

3. **Single Event Entry Point & Superior Testability**:
   - All user intentions flow through one strict entry point: a single private `processEvent(event)` in the ViewModel featuring an exhaustive `when` block (with no `else -> {}` fallback).
   - Unit testing the ViewModel is remarkably clean: tests invoke `state.value.eventTunnel(event)` and assert directly on state transitions without needing complex coroutine test channels or Turbine flow collectors for side effects.

4. **Zero Prop Drilling via CompositionLocal**:
   - In traditional architectures, passing click handlers or event callbacks down a deep Composable hierarchy requires tedious prop drilling across intermediate components.
   - **Horizon Solution**: By exposing the event tunnel via `CompositionLocalProvider(LocalHomeScreenEventTunnel provides state.eventTunnel)`, any child component at any depth can dispatch intents instantly (`LocalHomeScreenEventTunnel.current(event)`), keeping child Composables 100% stateless and trivial to preview.

* **Why it fits this project specifically**: The app requires synchronizing the Home widget and Light Drawer from a single source of truth, tracking exact error origins (widget vs drawer), and handling high-frequency continuous brightness gestures. Horizon's unified state snapshot and centralized `eventTunnel` make this synchronization and error attribution trivial without prop drilling.

---

## Key Architectural Decisions & Trade-offs

For an exhaustive evaluation of all architectural choices, alternatives considered, and trade-off rationales (*the "Why"*), please refer to **[docs/DECISIONS.md](docs/DECISIONS.md)**. A summary of core trade-offs includes:

1. **Optimistic UI with Rollback to Last Confirmed State**:
   - *Choice*: Updates UI immediately and rolls back on failure to `confirmedLights[id]`.
   - *Why / Trade-off*: Guarantees 0ms UX responsiveness while ensuring absolute multi-light concurrency safety across overlapping actions.
2. **Brightness Debounce (300 ms)**:
   - *Choice*: Throttles/debounces slider and swipe changes with a 300ms window, transmitting only the final settled value.
   - *Why / Trade-off*: Prevents network flooding while preserving buttery-smooth 60/120 FPS local visual feedback.
3. **No UseCases**:
   - *Choice*: ViewModel talks directly to `LightsRepository`.
   - *Why / Trade-off*: Avoids speculative indirection and boilerplate in a single-screen app, keeping the repository cleanly abstracted behind an interface.
4. **Customizable Failure Rate (0% to 100%)**:
   - *Choice*: Interactive header pill to cycle error probabilities live.
   - *Why / Trade-off*: Makes manual QA and live demoing of error rollback and toast behaviors instantly reproducible.

---

## How to Run & Test

1. Clone or open the project in **Android Studio**.
2. Run all unit and instrumentation tests via Gradle:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. Run the app on an emulator or physical device (`:app:assembleDebug`).

---

## How to Trigger Errors (Demo Mode)

- Tap the **"Fail: X%"** pill in the top header to cycle through failure rates (`0%`, `10%`, `50%`, `100%`).
- At `100%`, any toggle, brightness adjustment, or drawer open action will fail, triggering the error toast ("Something went wrong · Try again later"), rolling back the optimistic UI state, and displaying the floating red toast.
- Tapping any new action or the close (×) icon immediately dismisses the error toast.

---

## AI Coding Tools Usage & Refinements

As requested by the deliverables in `requirements.pdf`, here is how AI coding tools were utilized and refined during development:
- **Tools Used**: AI assistance integrated within the IDE.
- **Parts Assisted**: Scaffolding initial boilerplate structures for Koin DI modules, setting up coroutine test dispatchers (`StandardTestDispatcher`), and creating base Compose layout hierarchies.
- **Human Corrections & Refinements**:
  - *Coroutine Virtual Time Racing*: AI-generated test code initially used `advanceUntilIdle()` which prematurely consumed the 4-second auto-dismiss error delay timer during state verification. This was manually corrected by replacing it with `testDispatcher.scheduler.runCurrent()` to inspect immediate error state flags before advancing virtual time.
  - *Debounce Cancellation Handling*: AI implementations initially treated cancelled debounced jobs (during continuous slider dragging) as network service errors. This was manually corrected by propagating `CancellationException` and filtering it out in the ViewModel so that rapid slider/swipe dragging does not trigger false error toasts.
  - *UI Anatomy & Touch Targets*: Fine-tuned Compose padding, positioning of chevron touch targets (48x48 dp with internal right-edge alignment), and card border radii (`16.dp` container vs `10.dp` inner cards) to match Figma design specifications.

---

## What I'd Do With More Time (and in a realistic scenario)

### 1. Local-First Architecture & Persistence
- **Room Database Caching**: Implement local-first persistence using Jetpack Room to enable **instant cold starts (0ms UX)** via the *Stale-While-Revalidate* pattern. The app renders the last known state immediately from local storage while syncing with the local gateway in the background.
- **Offline Sync Queue (Outbox Pattern)**: Enable offline light control by persisting user commands in a local transactional outbox queue when network connectivity to the local gateway drops; commands automatically flush and resolve state conflicts (via vector clocks / ETags) upon reconnection.

### 2. Network Optimization & Hardware Telemetry
- **MQTT / WebSockets Real-Time Sync**: Replace REST polling with persistent WebSocket or MQTT connections to stream sub-100ms hardware telemetry and state updates (handling external events like physical wall switches changing light states).
- **Command Batching & Throttling**: Group rapid multi-bulb brightness commands into single batched payloads to prevent network congestion.
- **Heartbeat & Exponential Backoff**: Robust reconnection strategies with keep-alive heartbeats to handle flaky home Wi-Fi or gateway disconnections gracefully.

### 3. Advanced Spatial & Contextual UI
- **Interactive House Zoning**: Make the 3D house illustration interactive with touch hotspots mapped to specific rooms (Living Room, Kitchen, Bedroom, etc.) to filter and highlight zones on tap.
- **Motion & Shared Element Transitions**: Implement smooth shared element animations when opening/closing the Light Drawer and bottom sheet panels.

### 4. Widgets & Wear OS Companion
- **Jetpack Glance Home Screen Widgets**: Build interactive Glance widgets to toggle favorite lights directly from the Android home screen without opening the app.
- **Wear OS Companion App**: Add a companion Wear OS module for quick wrist-based lighting control.

### 5. Observability & Testing
- **Compose Screenshot Testing**: Add Paparazzi or Roborlectric screenshot testing to prevent visual regressions across themes and device form factors.
- **Crashlytics & Diagnostics**: Integrate crash reporting and analytics, like Firebase Crashlytics.
