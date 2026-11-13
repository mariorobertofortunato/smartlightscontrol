# Step 0 — Requirements mapped to the design

The Figma file could not be opened. The design references are screenshots:

| Name used below | What it shows | In the project? |
|---|---|---|
| **Home** | Bed light widget, dimmable, on, 75% | `homescreen_screenshot.png` |
| **Drawer** | Bed light drawer, dimmable, "Turn off", slider at 0% | `light_model_open.png` |
| **Home error** | Home with the error toast | Chat attachment only |
| **Drawer error** | Dimmable drawer at 100% with the error toast | Chat attachment only |
| **Home non-dimmable** | Widget showing "On", no percentage | Chat attachment only |
| **Drawer non-dimmable** | Drawer with "This is a non-dimmable light" | Chat attachment only |

States with no visual reference: every **off** state (widget "Off · 0%", non-dimmable
widget "Off", drawer "Turn on" with the unlit bulb).

## Acceptance criteria

| # | Surface | Action | Expected behaviour | Design reference |
|---|---|---|---|---|
| 1 | Widget | Tap (light off) | Turns on, restores last brightness, label "On · 75%" | Home shows the resulting state. Off state not seen. |
| 2 | Widget | Tap (light on) | Turns off, label "Off · 0%", brightness remembered | Off state not seen. |
| 3 | Widget | Swipe right / left | Brightness up / down, label and fill update live | Home: the fill covers the left 75% of the widget. |
| 4 | Widget | Tap arrow | Drawer opens for that light | Home shows the arrow; Drawer shows the result. |
| 5 | Drawer | Tap "Turn on" / "Turn off" | Toggles; button label, status text and bulb update | Drawer shows "Turn off" and a lit bulb. Drawer error shows the status "On" as the title. "Turn on" state not seen. |
| 6 | Drawer | Drag slider 0–100% | Brightness updates, bulb reflects it (min/max images) | Drawer (0%) and Drawer error (100%, track filled blue). |
| 7 | Drawer | Close | Widget reflects the latest state | No dedicated screen; follows from the single source of truth. |
| 8 | Widget, non-dimmable | Tap or swipe | Toggles; only "On" / "Off", no percentage | Home non-dimmable: label "On", the whole widget is filled. Off state not seen. |
| 9 | Drawer, non-dimmable | Open | No slider; "This is a non-dimmable light"; only the toggle button | Drawer non-dimmable. |
| 10 | Widget | Any action fails | Rolls back; "Something went wrong · Try again later" | Home error. |
| 11 | Drawer | Fails to open, or action fails | Same error style; rolls back | Drawer error (action failure). Home error covers a failure to open, since the drawer is not shown. |

## Product rules

- **Widget placement**: if the latest action was turning a light on, the widget offers the
  opposite action. Every Home screenshot shows a single widget whose tap toggles the light;
  no separate placement or ordering is visible.
- **Dimmable indicator**: the percentage on the widget signals that the light is dimmable.
  Home non-dimmable confirms it: the label is "On" with no percentage.

## What the screenshots show

### Home

- Header: house icon + "At home" on the left, "Pair buttons" pill and a "…" button on the right.
- House illustration, centred, below the header.
- One container holding a 2×2 grid:
  - Light widget: amber bulb icon, "Bed light", "On · 75%", arrow on the right. The lighter
    blue fill covers the left 75% of the widget.
  - "Entrance window" / "Closed", with arrow.
  - "Close all blinds", with arrow.
  - "Night time", with arrow.
- Image carousel in its own container: one image labelled "External", the next one peeking
  on the right.
- A down chevron.
- "Message Qe'ra…" input with a round microphone button.

### Home non-dimmable

- Same screen. The widget label is "On" and the lighter blue fill covers the whole widget.
- The white dot and line across the widget are a Figma prototype annotation (the swipe),
  not part of the UI.

### Drawer (dimmable)

- Bottom sheet covering roughly the lower 75% of the screen, with a drag handle. Home is
  dimmed behind it.
- Light name ("Bed light") above a title. The title reads "Device status" in Drawer and
  "On" in Drawer error.
- A panel with the bulb image and a glow.
- "Brightness" label with the current value on the right.
- Slider with "0%" and "100%" labels under its ends. The part left of the thumb is blue.
- Full-width pill button "Turn off".

### Drawer non-dimmable

- Same sheet, shorter: it wraps its content.
- Title "Device status".
- Instead of the brightness block: an info icon followed by "This is a non-dimmable light".
- Full-width pill button "Turn off".

### Error toast (Home error, Drawer error)

- A red rounded rectangle, horizontally centred, near the bottom of the screen.
- Two lines: "Something went wrong" and, smaller, "Try again later".
- A round close (×) icon on the right.
- It floats above the content: over the message input on Home, over the button in the drawer.
- The widget and the drawer themselves show no error styling.
