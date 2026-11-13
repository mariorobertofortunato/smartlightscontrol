package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light

class MockLightService : LightService {
    private val seedLights = listOf(
        Light(
            id = "bed_light",
            name = "Bed light",
            dimmable = true,
            isOn = true,
            brightness = 75,
            lastBrightness = 75
        ),
        Light(
            id = "group_all",
            name = "All the lights in the house",
            dimmable = false,
            isOn = true,
            brightness = 100,
            lastBrightness = 100
        ),
        Light(
            id = "entrance_light",
            name = "Entrance light",
            dimmable = false,
            isOn = false,
            brightness = 0,
            lastBrightness = 100
        )
    )

    override suspend fun list(): List<Light> = seedLights

    override suspend fun get(id: String): Light =
        seedLights.first { it.id == id }

    override suspend fun setPower(id: String, isOn: Boolean): Light {
        val light = get(id)
        return light.copy(isOn = isOn)
    }

    override suspend fun setBrightness(id: String, value: Int): Light {
        val light = get(id)
        return light.copy(brightness = value)
    }
}
