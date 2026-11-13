# Design tokens

Source: the two design screenshots (`homescreen_screenshot.png`, `light_model_open.png`,
330×711 px). The Figma file could not be opened, so nothing here comes from Figma variables.

- **Colours** were sampled from the screenshot pixels. Surfaces are reliable to within a few
  shades. Text colours are the brightest pixel found in each text run, so the real values are
  probably slightly lighter.
- **Sizes, spacing, radii and typography** are estimates. They were measured in screenshot
  pixels and scaled by ~1.19 (assuming a 393 pt wide source frame), then rounded.
- **Font family** is unknown. The system default sans-serif (Roboto) is assumed.
- **Error toast and non-dimmable states** come from screenshots that are not in the project.
  They could only be viewed, not sampled, so their colours are estimated by eye.

Replace any value with the Figma one when available.

## Colours

### Surfaces

| Token | Value | Where |
|---|---|---|
| `background` | `#191D20` (gradient `#1C1F23` → `#161A1D`) | Screen background |
| `surfaceContainer` | `#20262A` | Container around the 2×2 widget grid |
| `surfaceCard` | `#272F35` (up to `#2D353B`) | Placeholder cards (blinds, scenes) |
| `surfaceImageContainer` | `#252A2E` | Container around the image carousel |
| `surfaceInput` | `#1B1F22` | "Message Qe'ra…" input (has a thin lighter outline) |
| `surfacePill` | `#384046` | "Pair buttons" pill |
| `surfaceSheet` | `#272B2F` (top edge `#21272A`) | Light Drawer sheet |
| `surfacePanel` | `#2A3237` → `#222A2F` (vertical gradient) | Bulb panel in the drawer |

### Light widget

| Token | Value | Where |
|---|---|---|
| `widgetFill` | `#204259` | Filled part of the widget (brightness) |
| `widgetTrack` | `#172935` | Unfilled part of the widget |
| `widgetIconOn` | `#C59C54` | Bulb icon, light on |

### Accent and controls

| Token | Value | Where |
|---|---|---|
| `primary` | `#3B81B3` (gradient `#3C81B3` → `#3377A9`) | "Turn off" button, slider thumb |
| `sliderTrack` | `#717D89` | Slider track, right of the thumb |
| `sliderTrackActive` | same as `primary` (by eye) | Slider track, left of the thumb |
| `sliderThumbRing` | `#C1CED6` | Ring around the slider thumb |
| `sheetHandle` | `#77848F` | Drawer drag handle |
| `micButton` | `#D3D6D8` | Microphone button background |

### Error toast (estimated by eye)

| Token | Value | Where |
|---|---|---|
| `errorContainer` | `#9B2C2C` | Toast background |
| `onErrorContainer` | `#F5EDED` | Toast text and close icon |

### Text

| Token | Value (sampled) | Where |
|---|---|---|
| `textPrimary` | `#D9DCDD` | Card titles, "Device status", "Brightness", button label |
| `textSecondary` | `#BBC1C6` | Light name in the drawer, header "At home" |
| `textTertiary` | `#9AA4AC` | "Closed", slider range labels, input placeholder |
| `textOnWidget` | `#ABC2D2` | "On · 75%" (blue-tinted secondary) |

## Typography (estimated)

| Token | Size / weight | Where |
|---|---|---|
| `titleLarge` | 16 sp / semibold | "Device status" |
| `titleMedium` | 14 sp / medium | Card titles, "Brightness", brightness value, button label |
| `labelLarge` | 14 sp / semibold | "Pair buttons" |
| `bodySmall` | 12 sp / regular | Card subtitles ("On · 75%", "Closed"), light name in drawer, "At home", "External" |
| `labelSmall` | 10 sp / regular | Slider range labels ("0%", "100%") |

## Spacing (estimated)

| Token | Value | Where |
|---|---|---|
| `screenPadding` | 20 dp | Home horizontal margin |
| `sheetPadding` | 16 dp | Drawer horizontal margin |
| `gridPadding` | 8 dp | Inside the grid container, and the gap between cards |
| `cardPadding` | 12 dp | Inside a card |
| `sectionGap` | 16 dp | Between grid container and image container |

## Sizes (estimated)

| Element | Value |
|---|---|
| Widget / card | ~167 × 107 dp (two per row) |
| House image | ~95 dp tall, centred |
| Image carousel | ~225 dp tall, next image peeking on the right |
| Bulb panel | ~310 dp tall, full sheet width |
| Drawer button | full width, 48 dp tall (44 dp in the screenshot; raised to meet the touch target) |
| Slider thumb | ~30 dp visible, 48 dp touch target |
| Sheet handle | 48 × 4 dp |
| Message input | ~42 dp tall; mic button ~42 dp circle with 48 dp touch target |
| Error toast | ~235 × 70 dp, centred, ~40 dp above the bottom edge, radius 10 dp; title in `titleMedium`, second line in `bodySmall`; close icon ~16 dp with 48 dp touch target |
| Non-dimmable info row | info icon ~16 dp + text in `bodySmall`, in place of the brightness block |

## Radii (estimated)

| Token | Value | Where |
|---|---|---|
| `radiusContainer` | 16 dp | Grid container, image container, sheet top corners |
| `radiusCard` | 10 dp | Cards, widget, bulb panel, carousel image |
| `radiusPill` | 50% | Buttons, "Pair buttons", message input, mic button |
