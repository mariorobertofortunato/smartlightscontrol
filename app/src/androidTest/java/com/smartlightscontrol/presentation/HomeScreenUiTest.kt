package com.smartlightscontrol.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun widgetTapDispatchesTogglePowerEventThroughTunnel() {
        var toggledId: String? = null
        val lights = listOf(
            Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
        )

        composeTestRule.setContent {
            SmartLightsControlTheme {
                HomeScreenContent(
                    state = HomeScreenState(
                        lights = lights,
                        selectedLightId = "bed_light",
                        eventTunnel = { event ->
                            if (event is HomeScreenEvent.TogglePower) {
                                toggledId = event.id
                            }
                        }
                    )
                )
            }
        }

        // Verify initial light label and status are displayed
        composeTestRule.onNodeWithText("Bed light").assertIsDisplayed()
        composeTestRule.onNodeWithText("On · 75%").assertIsDisplayed()

        // Perform click on the widget card
        composeTestRule.onNodeWithContentDescription("Bed light, On · 75%").performClick()

        // Verify that the TogglePower event was successfully sent through the CompositionLocal event tunnel
        assertEquals("bed_light", toggledId)
    }

    @Test
    fun errorToastIsDisplayedWhenWidgetErrorIsTrue() {
        val lights = listOf(
            Light("bed_light", "Bed light", dimmable = true, isOn = true, brightness = 75, lastBrightness = 75)
        )

        composeTestRule.setContent {
            SmartLightsControlTheme {
                HomeScreenContent(
                    state = HomeScreenState(
                        lights = lights,
                        selectedLightId = "bed_light",
                        widgetError = true,
                        errorMessage = "Something went wrong · Try again later"
                    )
                )
            }
        }

        // Verify that the error toast message is displayed on screen
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again later").assertIsDisplayed()
    }

    @Test
    fun nonDimmableWidgetSwipeDispatchesTogglePowerEvent() {
        var toggledId: String? = null
        val lights = listOf(
            Light("entrance_light", "Entrance light", dimmable = false, isOn = false, brightness = 0, lastBrightness = 100)
        )

        composeTestRule.setContent {
            SmartLightsControlTheme {
                HomeScreenContent(
                    state = HomeScreenState(
                        lights = lights,
                        selectedLightId = "entrance_light",
                        eventTunnel = { event ->
                            if (event is HomeScreenEvent.TogglePower) {
                                toggledId = event.id
                            }
                        }
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("Entrance light").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Entrance light, Off").performTouchInput {
            swipeRight()
        }

        assertEquals("entrance_light", toggledId)
    }
}
