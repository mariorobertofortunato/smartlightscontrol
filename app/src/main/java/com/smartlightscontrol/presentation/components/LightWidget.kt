package com.smartlightscontrol.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.presentation.HomeScreenEvent
import com.smartlightscontrol.presentation.LocalHomeScreenEventTunnel
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme
import com.smartlightscontrol.ui.theme.TextOnWidget
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.WidgetFill
import com.smartlightscontrol.ui.theme.WidgetIconOff
import com.smartlightscontrol.ui.theme.WidgetIconOn
import com.smartlightscontrol.ui.theme.WidgetTrack

@Composable
fun LightWidget(
    light: Light,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current
    val currentLight by rememberUpdatedState(light)
    val currentEventTunnel by rememberUpdatedState(eventTunnel)

    val fillFractionTarget = when {
        !light.isOn -> 0f
        !light.dimmable -> 1f
        else -> (light.brightness / 100f).coerceIn(0f, 1f)
    }

    val animatedFillFraction by animateFloatAsState(
        targetValue = fillFractionTarget,
        label = "light_widget_fill_animation"
    )

    val statusText = when {
        !light.dimmable -> if (light.isOn) "On" else "Off"
        light.isOn -> "On · ${light.brightness}%"
        else -> "Off · 0%"
    }

    var cardWidthPx by remember { mutableFloatStateOf(1f) }
    var dragAccumulatorPx by remember { mutableFloatStateOf(0f) }

    val cornerRadius = 10.dp // Inner card radius nesting inside 16.dp container

    Box(
        modifier = modifier
            .height(107.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(WidgetTrack)
            .border(1.dp, Color(0xFF2A3237), RoundedCornerShape(cornerRadius))
            .onGloballyPositioned { coordinates ->
                cardWidthPx = coordinates.size.width.toFloat().coerceAtLeast(1f)
            }
            .semantics(mergeDescendants = false) {
                role = Role.Switch
                this.stateDescription = statusText
                this.contentDescription = "${light.name}, $statusText"
            }
            .pointerInput(light.id) {
                detectTapGestures(
                    onTap = {
                        currentEventTunnel(HomeScreenEvent.TogglePower(currentLight.id))
                    }
                )
            }
            .pointerInput(light.id) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val touchRatio = (offset.x / cardWidthPx).coerceIn(0f, 1f)
                        dragAccumulatorPx = touchRatio * cardWidthPx
                        if (currentLight.dimmable) {
                            val newBrightness = (touchRatio * 100).toInt().coerceIn(0, 100)
                            currentEventTunnel(HomeScreenEvent.ChangeBrightness(currentLight.id, newBrightness))
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (currentLight.dimmable) {
                            dragAccumulatorPx = (dragAccumulatorPx + dragAmount).coerceIn(0f, cardWidthPx)
                            val newBrightness = ((dragAccumulatorPx / cardWidthPx) * 100).toInt().coerceIn(0, 100)
                            currentEventTunnel(HomeScreenEvent.ChangeBrightness(currentLight.id, newBrightness))
                        } else {
                            if (dragAmount > 15f && !light.isOn) {
                                currentEventTunnel(HomeScreenEvent.TogglePower(currentLight.id))
                            } else if (dragAmount < -15f && light.isOn) {
                                currentEventTunnel(HomeScreenEvent.TogglePower(currentLight.id))
                            }
                        }
                    }
                )
            }
    ) {
        // Dynamic Fill Portion
        if (animatedFillFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFillFraction)
                    .background(WidgetFill)
            )
        }

        // Content Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
        ) {
            // Top-Left: Bulb Icon with left padding
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = if (light.isOn) WidgetIconOn else WidgetIconOff,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
            )

            // Center-Right: Chevron touch target Box flush against right edge with centered icon
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        eventTunnel(HomeScreenEvent.OpenDrawer(light.id))
                    }
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextOnWidget,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom-Left: Name & Status with left padding
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = light.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnWidget,
                    maxLines = 1
                )
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Dimmable ON (75%)")
@Composable
fun LightWidgetDimmableOnPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
                LightWidget(
                    light = Light(
                        id = "bed_light",
                        name = "Bed light",
                        dimmable = true,
                        isOn = true,
                        brightness = 75,
                        lastBrightness = 75
                    ),
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }
    }
}

@Preview(name = "Dimmable OFF (0%)")
@Composable
fun LightWidgetDimmableOffPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
                LightWidget(
                    light = Light(
                        id = "bed_light",
                        name = "Bed light",
                        dimmable = true,
                        isOn = false,
                        brightness = 0,
                        lastBrightness = 75
                    ),
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }
    }
}

@Preview(name = "Non-Dimmable ON")
@Composable
fun LightWidgetNonDimmableOnPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
                LightWidget(
                    light = Light(
                        id = "group_all",
                        name = "All the lights in the house",
                        dimmable = false,
                        isOn = true,
                        brightness = 100,
                        lastBrightness = 100
                    ),
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }
    }
}

@Preview(name = "Non-Dimmable OFF")
@Composable
fun LightWidgetNonDimmableOffPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
                LightWidget(
                    light = Light(
                        id = "entrance_light",
                        name = "Entrance light",
                        dimmable = false,
                        isOn = false,
                        brightness = 0,
                        lastBrightness = 100
                    ),
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }
    }
}
