package com.smartlightscontrol.presentation

import androidx.lifecycle.ViewModel
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.domain.LightsRepository
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    repository: LightsRepository
) : ViewModel() {
    val lights: StateFlow<List<Light>> = repository.lights
}
