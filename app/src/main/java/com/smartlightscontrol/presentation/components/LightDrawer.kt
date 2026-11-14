package com.smartlightscontrol.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.R
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.presentation.HomeScreenEvent
import com.smartlightscontrol.presentation.HomeScreenState
import com.smartlightscontrol.presentation.LocalHomeScreenEventTunnel
import com.smartlightscontrol.ui.theme.PrimaryAccent
import com.smartlightscontrol.ui.theme.SheetHandle
import com.smartlightscontrol.ui.theme.SliderThumbRing
import com.smartlightscontrol.ui.theme.SliderTrack
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme
import com.smartlightscontrol.ui.theme.SurfacePanelBottom
import com.smartlightscontrol.ui.theme.SurfacePanelTop
import com.smartlightscontrol.ui.theme.SurfacePill
import com.smartlightscontrol.ui.theme.SurfaceSheet
import com.smartlightscontrol.ui.theme.TextPrimary
import com.smartlightscontrol.ui.theme.TextSecondary
import com.smartlightscontrol.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightDrawer(
    state: HomeScreenState,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current
    val drawerLight = state.drawerLight

    if (state.isDrawerOpen && drawerLight != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                eventTunnel(HomeScreenEvent.CloseDrawer)
            },
            sheetState = sheetState,
            containerColor = SurfaceSheet,
            contentColor = TextPrimary,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = SheetHandle)
            },
            modifier = modifier
        ) {
            LightDrawerContent(
                light = drawerLight,
                drawerError = state.drawerError,
                errorMessage = state.errorMessage
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightDrawerContent(
    light: Light,
    drawerError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val eventTunnel = LocalHomeScreenEventTunnel.current
    val context = LocalContext.current

    val bulbBitmap = remember(context, light.isOn, light.brightness) {
        val resourceId = if (light.isOn && (light.brightness > 0 || !light.dimmable)) {
            R.raw.bulb_on
        } else {
            R.raw.bulb_off
        }
        BitmapFactory.decodeResource(context.resources, resourceId)
    }

    // Per Decision #5: Status title shows current state ("On" / "Off")
    val statusTitle = if (light.isOn) "On" else "Off"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info: Light Name (subtitle) & Status Title ("On" / "Off" per Decision #5)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = light.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bulb Panel Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(SurfacePanelTop, SurfacePanelBottom)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bulbBitmap != null) {
                    Image(
                        bitmap = bulbBitmap.asImageBitmap(),
                        contentDescription = if (light.isOn) "Bulb glowing on" else "Bulb off",
                        modifier = Modifier.size(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls Section: Brightness Slider vs Non-Dimmable Info
            if (light.dimmable) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brightness",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${light.brightness}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Custom Slider matching Figma design tokens & screenshot
                    Slider(
                        value = light.brightness.toFloat(),
                        onValueChange = { newValue ->
                            eventTunnel(HomeScreenEvent.ChangeBrightness(light.id, newValue.toInt()))
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryAccent,
                            activeTrackColor = PrimaryAccent,
                            inactiveTrackColor = SliderTrack
                        ),
                        thumb = {
                            // Custom Thumb with Ring matching Figma
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SliderThumbRing)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryAccent)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(8.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = PrimaryAccent,
                                    inactiveTrackColor = SliderTrack
                                ),
                                drawStopIndicator = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                this.stateDescription = "${light.brightness}% brightness"
                                this.contentDescription = "Adjust brightness slider"
                            }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                // Non-Dimmable Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This is a non-dimmable light",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Power Button (Full-width pill button, 48dp height touch target)
            val isButtonOn = light.isOn
            Button(
                onClick = {
                    eventTunnel(HomeScreenEvent.TogglePower(light.id))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonOn) PrimaryAccent else SurfacePill,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        this.contentDescription = if (isButtonOn) "Turn off ${light.name}" else "Turn on ${light.name}"
                    }
            ) {
                Text(
                    text = if (isButtonOn) "Turn off" else "Turn on",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Drawer Error Toast Overlay
        AnimatedVisibility(
            visible = drawerError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            ErrorToast(errorMessage = errorMessage)
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Drawer - Dimmable ON (75%)")
@Composable
fun LightDrawerDimmableOnPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = SurfaceSheet) {
                LightDrawerContent(
                    light = Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75),
                    drawerError = false,
                    errorMessage = null
                )
            }
        }
    }
}

@Preview(name = "Drawer - Dimmable OFF (0%)")
@Composable
fun LightDrawerDimmableOffPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = SurfaceSheet) {
                LightDrawerContent(
                    light = Light("bed_light", "Bed light", dimmable = true, isOn = false, brightness = 0, lastBrightness = 75),
                    drawerError = false,
                    errorMessage = null
                )
            }
        }
    }
}

@Preview(name = "Drawer - Non-Dimmable ON")
@Composable
fun LightDrawerNonDimmableOnPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = SurfaceSheet) {
                LightDrawerContent(
                    light = Light("group_all", "All the lights in the house", dimmable = false, isOn = true, brightness = 100, lastBrightness = 100),
                    drawerError = false,
                    errorMessage = null
                )
            }
        }
    }
}

@Preview(name = "Drawer - Non-Dimmable OFF")
@Composable
fun LightDrawerNonDimmableOffPreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = SurfaceSheet) {
                LightDrawerContent(
                    light = Light("entrance_light", "Entrance light", dimmable = false, isOn = false, brightness = 0, lastBrightness = 100),
                    drawerError = false,
                    errorMessage = null
                )
            }
        }
    }
}

@Preview(name = "Drawer - Error Toast State")
@Composable
fun LightDrawerErrorStatePreview() {
    SmartLightsControlTheme {
        CompositionLocalProvider(LocalHomeScreenEventTunnel provides {}) {
            Surface(color = SurfaceSheet) {
                LightDrawerContent(
                    light = Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 100, lastBrightness = 100),
                    drawerError = true,
                    errorMessage = "Something went wrong · Try again later"
                )
            }
        }
    }
}
