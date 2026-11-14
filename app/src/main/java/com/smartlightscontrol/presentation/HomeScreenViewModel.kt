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
        // Automatically dismiss any active error banner as soon as the user triggers a new action
        if (event !is HomeScreenEvent.DismissError && _state.value.hasError) {
            clearError()
        }

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

            is HomeScreenEvent.CycleFailureRate -> {
                val nextRate = when (_state.value.failureRate) {
                    0.0 -> 0.1
                    0.1 -> 0.5
                    0.5 -> 1.0
                    else -> 0.0
                }
                repository.setFailureRate(nextRate)
                _state.update { it.copy(failureRate = nextRate) }
            }
        }
    }

    private fun handleTogglePower(id: String) {
        val isDrawerOrigin = _state.value.isDrawerOpen && _state.value.drawerLightId == id
        val targetLight = _state.value.lights.firstOrNull { id == "group_all" || it.id == id } ?: return
        val newPower = if (id == "group_all") !_state.value.lights.all { it.isOn } else !targetLight.isOn

        viewModelScope.launch {
            val result = repository.setPower(id, newPower)
            val exception = result.exceptionOrNull()
            if (result.isFailure && exception !is kotlinx.coroutines.CancellationException) {
                setError(isDrawerOrigin)
            }
        }
    }

    private fun handleChangeBrightness(id: String, brightness: Int) {
        val isDrawerOrigin = _state.value.isDrawerOpen && _state.value.drawerLightId == id

        viewModelScope.launch {
            val result = repository.setBrightness(id, brightness)
            val exception = result.exceptionOrNull()
            if (result.isFailure && exception !is kotlinx.coroutines.CancellationException) {
                setError(isDrawerOrigin)
            }
        }
    }

    private fun handleOpenDrawer(id: String) {
        val exists = _state.value.lights.any { it.id == id }
        if (!exists) {
            setError(isDrawerOrigin = false)
            return
        }

        // Optimistically open drawer instantly
        _state.update {
            it.copy(
                isDrawerOpen = true,
                drawerLightId = id
            )
        }

        // Background sync / verify fetch
        viewModelScope.launch {
            val result = repository.getLight(id)
            if (result.isFailure && _state.value.isDrawerOpen && _state.value.drawerLightId == id) {
                // If background fetch fails, close drawer and show error toast
                _state.update { it.copy(isDrawerOpen = false, drawerLightId = null) }
                setError(isDrawerOrigin = false)
            }
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
