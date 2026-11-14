package com.smartlightscontrol.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import com.smartlightscontrol.domain.Light

sealed interface HomeScreenEvent {
    data class SelectLight(val id: String) : HomeScreenEvent
    data class TogglePower(val id: String) : HomeScreenEvent
    data class ChangeBrightness(val id: String, val brightness: Int) : HomeScreenEvent
    data class OpenDrawer(val id: String) : HomeScreenEvent
    object CloseDrawer : HomeScreenEvent
    object DismissError : HomeScreenEvent
    object CycleFailureRate : HomeScreenEvent
}

data class HomeScreenState(
    val lights: List<Light> = emptyList(),
    val selectedLightId: String = "bed_light",
    val isDrawerOpen: Boolean = false,
    val drawerLightId: String? = null,
    val widgetError: Boolean = false,
    val drawerError: Boolean = false,
    val errorMessage: String? = null,
    val failureRate: Double = 0.1,
    val eventTunnel: (HomeScreenEvent) -> Unit = {}
) {
    val selectedLight: Light?
        get() = lights.firstOrNull { it.id == selectedLightId }

    val drawerLight: Light?
        get() = drawerLightId?.let { id -> lights.firstOrNull { it.id == id } }

    val hasError: Boolean
        get() = widgetError || drawerError

    val failureRatePercent: Int
        get() = (failureRate * 100).toInt()
}

val LocalHomeScreenEventTunnel = staticCompositionLocalOf<(HomeScreenEvent) -> Unit> {
    error("LocalHomeScreenEventTunnel not provided")
}
