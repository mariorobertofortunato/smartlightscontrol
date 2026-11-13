package com.smartlightscontrol.domain

import kotlinx.coroutines.flow.StateFlow

interface LightsRepository {
    val lights: StateFlow<List<Light>>

    suspend fun getLights(): Result<List<Light>>
    suspend fun setPower(id: String, isOn: Boolean): Result<Light>
    suspend fun setBrightness(id: String, value: Int): Result<Light>
}
