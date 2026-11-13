package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.domain.LightsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LightsRepositoryImpl(
    private val service: LightService
) : LightsRepository {

    private val _lights = MutableStateFlow<List<Light>>(emptyList())
    override val lights: StateFlow<List<Light>> = _lights.asStateFlow()

    init {
        // Initial seed data loading for skeleton
        _lights.value = listOf(
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
    }

    override suspend fun getLights(): Result<List<Light>> {
        return try {
            val fetched = service.list()
            _lights.value = fetched
            Result.success(fetched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setPower(id: String, isOn: Boolean): Result<Light> {
        return try {
            val updated = service.setPower(id, isOn)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setBrightness(id: String, value: Int): Result<Light> {
        return try {
            val updated = service.setBrightness(id, value)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
