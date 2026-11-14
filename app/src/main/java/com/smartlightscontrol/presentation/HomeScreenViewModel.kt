package com.smartlightscontrol.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartlightscontrol.domain.LightsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class HomeScreenViewModel(
    private val repository: LightsRepository,
    private val autoDismissDelayMs: Long = 4000L
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeScreenState(
            eventTunnel = { processEvent(it) }
        )
    )
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var errorDismissJob: Job? = null

    init {
        viewModelScope.launch {
            repository.lights.collect { newLights ->
                _state.update { current ->
                    // Keep selectedLightId valid if current selection is missing
                    val validSelectedId = if (newLights.any { it.id == current.selectedLightId }) {
                        current.selectedLightId
                    } else {
                        newLights.firstOrNull()?.id ?: current.selectedLightId
                    }
                    current.copy(
                        lights = newLights,
                        selectedLightId = validSelectedId
                    )
                }
            }
        }
    }

    private fun processEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.SelectLight -> {
                _state.update { it.copy(selectedLightId = event.id) }
            }

            is HomeScreenEvent.TogglePower -> {
                handleTogglePower(event.id)
            }

            is HomeScreenEvent.ChangeBrightness -> {
                handleChangeBrightness(event.id, event.brightness)
            }

            is HomeScreenEvent.OpenDrawer -> {
                handleOpenDrawer(event.id)
            }

            is HomeScreenEvent.CloseDrawer -> {
                _state.update { it.copy(isDrawerOpen = false, drawerLightId = null) }
            }

            is HomeScreenEvent.DismissError -> {
                clearError()
            }
        }
    }

    private fun handleTogglePower(id: String) {
        val isDrawerOrigin = _state.value.isDrawerOpen && _state.value.drawerLightId == id
        val targetLight = _state.value.lights.firstOrNull { it.id == id } ?: return
        val newPower = !targetLight.isOn

        viewModelScope.launch {
            val result = repository.setPower(id, newPower)
            if (result.isFailure) {
                setError(isDrawerOrigin)
            }
        }
    }

    private fun handleChangeBrightness(id: String, brightness: Int) {
        val isDrawerOrigin = _state.value.isDrawerOpen && _state.value.drawerLightId == id

        viewModelScope.launch {
            val result = repository.setBrightness(id, brightness)
            if (result.isFailure) {
                setError(isDrawerOrigin)
            }
        }
    }

    private fun handleOpenDrawer(id: String) {
        val exists = _state.value.lights.any { it.id == id }
        if (exists) {
            _state.update {
                it.copy(
                    isDrawerOpen = true,
                    drawerLightId = id
                )
            }
        } else {
            setError(isDrawerOrigin = false)
        }
    }

    private fun setError(isDrawerOrigin: Boolean) {
        errorDismissJob?.cancel()
        _state.update {
            it.copy(
                widgetError = !isDrawerOrigin,
                drawerError = isDrawerOrigin,
                errorMessage = "Something went wrong · Try again later"
            )
        }
        if (autoDismissDelayMs > 0) {
            errorDismissJob = viewModelScope.launch {
                delay(autoDismissDelayMs.milliseconds)
                clearError()
            }
        }
    }

    private fun clearError() {
        errorDismissJob?.cancel()
        _state.update {
            it.copy(
                widgetError = false,
                drawerError = false,
                errorMessage = null
            )
        }
    }
}
