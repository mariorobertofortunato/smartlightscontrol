package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class MockLightService(
    var failureRate: Double = 0.1,
    private val random: Random = Random.Default,
    private val delayMillisProvider: () -> Long = { random.nextLong(300, 801) }
) : LightService {

    private val lightsMap = mutableMapOf(
        "bed_light" to Light(
            id = "bed_light",
            name = "Bed light",
            dimmable = true,
            isOn = true,
            brightness = 75,
            lastBrightness = 75
        ),
        "group_all" to Light(
            id = "group_all",
            name = "All the lights in the house",
            dimmable = false,
            isOn = true,
            brightness = 100,
            lastBrightness = 100
        ),
        "entrance_light" to Light(
            id = "entrance_light",
            name = "Entrance light",
            dimmable = false,
            isOn = false,
            brightness = 0,
            lastBrightness = 100
        )
    )

    private suspend fun simulateNetwork() {
        val delayMs = delayMillisProvider()
        if (delayMs > 0) {
            delay(delayMs.milliseconds)
        }
        if (failureRate > 0.0 && random.nextDouble() < failureRate) {
            throw IOException("Network request failed (simulated failure rate: $failureRate)")
        }
    }

    override suspend fun list(): List<Light> {
        simulateNetwork()
        return lightsMap.values.map { it.copy() }
    }

    override suspend fun get(id: String): Light {
        simulateNetwork()
        return lightsMap[id]?.copy() ?: throw NoSuchElementException("Light with id $id not found")
    }

    override suspend fun setPower(id: String, isOn: Boolean): Light {
        simulateNetwork()
        if (id == "group_all") {
            lightsMap.keys.forEach { lightId ->
                val target = lightsMap[lightId]!!
                val updated = if (!isOn) {
                    target.copy(isOn = false, brightness = 0)
                } else {
                    val restoredBrightness = if (target.lastBrightness > 0) target.lastBrightness else 75
                    target.copy(
                        isOn = true,
                        brightness = if (target.dimmable) restoredBrightness else 100,
                        lastBrightness = restoredBrightness
                    )
                }
                lightsMap[lightId] = updated
            }
            return lightsMap["group_all"]!!.copy()
        }

        val current = lightsMap[id] ?: throw NoSuchElementException("Light with id $id not found")
        val updated = if (!isOn) {
            current.copy(isOn = false, brightness = 0)
        } else {
            val restoredBrightness = if (current.lastBrightness > 0) current.lastBrightness else 75
            current.copy(
                isOn = true,
                brightness = if (current.dimmable) restoredBrightness else 100,
                lastBrightness = restoredBrightness
            )
        }
        lightsMap[id] = updated
        return updated.copy()
    }

    override suspend fun setBrightness(id: String, value: Int): Light {
        simulateNetwork()
        val current = lightsMap[id] ?: throw NoSuchElementException("Light with id $id not found")
        val clamped = value.coerceIn(0, 100)
        val updated = if (clamped == 0) {
            current.copy(isOn = false, brightness = 0)
        } else {
            current.copy(isOn = true, brightness = clamped, lastBrightness = clamped)
        }
        lightsMap[id] = updated
        return updated.copy()
    }
}
