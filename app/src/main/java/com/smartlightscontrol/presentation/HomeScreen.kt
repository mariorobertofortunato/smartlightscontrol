package com.smartlightscontrol.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.presentation.components.AssistantInputBar
import com.smartlightscontrol.presentation.components.CloseAllBlindsCard
import com.smartlightscontrol.presentation.components.EntranceWindowCard
import com.smartlightscontrol.presentation.components.ErrorToast
import com.smartlightscontrol.presentation.components.ExternalCarouselCard
import com.smartlightscontrol.presentation.components.HomeHeader
import com.smartlightscontrol.presentation.components.LightDrawer
import com.smartlightscontrol.presentation.components.LightSelectorRow
import com.smartlightscontrol.presentation.components.LightWidget
import com.smartlightscontrol.presentation.components.NightTimeCard
import com.smartlightscontrol.ui.theme.AppBackground
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme
import com.smartlightscontrol.ui.theme.SurfaceContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    HomeScreenContent(state = state)
}

@Composable
fun HomeScreenContent(
    state: HomeScreenState,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalHomeScreenEventTunnel provides state.eventTunnel) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Header Section
                        HomeHeader(failureRatePercent = state.failureRatePercent)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Light Selection Dropdown (Decision #1)
                        if (state.lights.isNotEmpty()) {
                            LightSelectorRow(
                                lights = state.lights,
                                selectedLightId = state.selectedLightId
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2x2 Grid Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainer)
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Row 1: Selected Light Widget + Entrance Window
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val selectedLight = state.selectedLight ?: state.lights.firstOrNull()
                                    if (selectedLight != null) {
                                        LightWidget(
                                            light = selectedLight,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    EntranceWindowCard(modifier = Modifier.weight(1f))
                                }

                                // Row 2: Blinds + Night Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CloseAllBlindsCard(modifier = Modifier.weight(1f))
                                    NightTimeCard(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // External Carousel Section
                        ExternalCarouselCard()
                    }

                    // Bottom Assistant Input Bar
                    AssistantInputBar(modifier = Modifier.padding(top = 12.dp))
                }

                // Error Toast Overlay for Main Widget Errors
                AnimatedVisibility(
                    visible = state.widgetError && !state.isDrawerOpen,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 70.dp)
                ) {
                    ErrorToast(errorMessage = state.errorMessage)
                }

                // Modal Bottom Sheet Light Drawer
                LightDrawer(state = state)
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Home - Dimmable Light ON (75%)")
@Composable
fun HomeScreenDimmableOnPreview() {
    val mockLights = listOf(
        Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75),
        Light("group_all", "All the lights in the house", dimmable = false, isOn = true, brightness = 100, lastBrightness = 100),
        Light("entrance_light", "Entrance light", dimmable = false, isOn = false, brightness = 0, lastBrightness = 100)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "bed_light"
            )
        )
    }
}

@Preview(name = "Home - Dimmable Light OFF (0%)")
@Composable
fun HomeScreenDimmableOffPreview() {
    val mockLights = listOf(
        Light("bed_light", "Bed light", dimmable = true, isOn = false, brightness = 0, lastBrightness = 75),
        Light("group_all", "All the lights in the house", dimmable = false, isOn = true, brightness = 100, lastBrightness = 100)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "bed_light"
            )
        )
    }
}

@Preview(name = "Home - Non-Dimmable Light ON")
@Composable
fun HomeScreenNonDimmableOnPreview() {
    val mockLights = listOf(
        Light("group_all", "All the lights in the house", dimmable = false, isOn = true, brightness = 100, lastBrightness = 100),
        Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "group_all"
            )
        )
    }
}

@Preview(name = "Home - Non-Dimmable Light OFF")
@Composable
fun HomeScreenNonDimmableOffPreview() {
    val mockLights = listOf(
        Light("entrance_light", "Entrance light", dimmable = false, isOn = false, brightness = 0, lastBrightness = 100),
        Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "entrance_light"
            )
        )
    }
}

@Preview(name = "Home - Widget Error Toast")
@Composable
fun HomeScreenErrorStatePreview() {
    val mockLights = listOf(
        Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "bed_light",
                widgetError = true,
                errorMessage = "Something went wrong · Try again later"
            )
        )
    }
}

@Preview(name = "Home - Light Drawer Open")
@Composable
fun HomeScreenDrawerOpenPreview() {
    val mockLights = listOf(
        Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
    )
    SmartLightsControlTheme {
        HomeScreenContent(
            state = HomeScreenState(
                lights = mockLights,
                selectedLightId = "bed_light",
                isDrawerOpen = true,
                drawerLightId = "bed_light"
            )
        )
    }
}
