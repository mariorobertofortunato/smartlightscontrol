package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light

interface LightService {
    suspend fun list(): List<Light>
    suspend fun get(id: String): Light
    suspend fun setPower(id: String, isOn: Boolean): Light
    suspend fun setBrightness(id: String, value: Int): Light
}
