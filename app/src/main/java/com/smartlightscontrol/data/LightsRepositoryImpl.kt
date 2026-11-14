package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.domain.LightsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

class LightsRepositoryImpl(
    private val service: LightService,
    private val debounceMillis: Long = 300L,
    coroutineContext: CoroutineContext = Dispatchers.Default
) : LightsRepository {

    private val repositoryScope = CoroutineScope(coroutineContext + Job())
    private val _lights = MutableStateFlow<List<Light>>(emptyList())
    override val lights: StateFlow<List<Light>> = _lights.asStateFlow()

    private val confirmedLights = ConcurrentHashMap<String, Light>()
    private val debounceJobs = ConcurrentHashMap<String, Job>()

    init {
        // Synchronously seed initial confirmed state if service supports synchronous list or on first getLights()
        val seed = listOf(
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
        seed.forEach { confirmedLights[it.id] = it }
        _lights.value = seed
    }

    override suspend fun getLights(): Result<List<Light>> {
        return try {
            val fetched = service.list()
            fetched.forEach { confirmedLights[it.id] = it }
            _lights.value = fetched
            Result.success(fetched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLight(id: String): Result<Light> {
        return try {
            val fetched = service.get(id)
            confirmedLights[fetched.id] = fetched
            _lights.update { currentList ->
                currentList.map { if (it.id == fetched.id) fetched else it }
            }
            Result.success(fetched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun setFailureRate(rate: Double) {
        (service as? MockLightService)?.failureRate = rate.coerceIn(0.0, 1.0)
    }

    override suspend fun setPower(id: String, isOn: Boolean): Result<Light> {
        // 1. Optimistic update
        _lights.update { currentList ->
            currentList.map { light ->
                if (id == "group_all" || light.id == id) {
                    if (!isOn) {
                        light.copy(isOn = false, brightness = 0)
                    } else {
                        val restoredBrightness = if (light.lastBrightness > 0) light.lastBrightness else 75
                        light.copy(
                            isOn = true,
                            brightness = if (light.dimmable) restoredBrightness else 100,
                            lastBrightness = restoredBrightness
                        )
                    }
                } else {
                    light
                }
            }
        }

        // 2. Service call & Rollback on failure
        return try {
            val confirmedServiceResult = service.setPower(id, isOn)
            if (id == "group_all") {
                // Re-fetch or update all confirmed
                val freshList = service.list()
                freshList.forEach { confirmedLights[it.id] = it }
                _lights.value = freshList
            } else {
                confirmedLights[confirmedServiceResult.id] = confirmedServiceResult
                _lights.update { currentList ->
                    currentList.map { if (it.id == confirmedServiceResult.id) confirmedServiceResult else it }
                }
            }
            Result.success(confirmedServiceResult)
        } catch (e: Exception) {
            // Rollback only the affected light(s) to confirmed state
            _lights.update { currentList ->
                currentList.map { light ->
                    if (id == "group_all" || light.id == id) {
                        confirmedLights[light.id] ?: light
                    } else {
                        light
                    }
                }
            }
            Result.failure(e)
        }
    }

    override suspend fun setBrightness(id: String, value: Int): Result<Light> {
        val clamped = value.coerceIn(0, 100)

        // 1. Immediate optimistic UI update
        _lights.update { currentList ->
            currentList.map { light ->
                if (light.id == id) {
                    if (clamped == 0) {
                        light.copy(isOn = false, brightness = 0)
                    } else {
                        light.copy(isOn = true, brightness = clamped, lastBrightness = clamped)
                    }
                } else {
                    light
                }
            }
        }

        // 2. Cancel prior debounce job for this light
        debounceJobs[id]?.cancel()

        var result: Result<Light>? = null
        val job = repositoryScope.launch {
            if (debounceMillis > 0) {
                delay(debounceMillis.milliseconds)
            }
            try {
                val updated = service.setBrightness(id, clamped)
                confirmedLights[updated.id] = updated
                _lights.update { currentList ->
                    currentList.map { if (it.id == updated.id) updated else it }
                }
                result = Result.success(updated)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Rollback affected light to confirmed state
                _lights.update { currentList ->
                    currentList.map { light ->
                        if (light.id == id) confirmedLights[id] ?: light else light
                    }
                }
                result = Result.failure(e)
            }
        }

        debounceJobs[id] = job
        try {
            job.join()
        } catch (e: kotlinx.coroutines.CancellationException) {
            return Result.failure(e)
        }

        return result ?: Result.failure(kotlinx.coroutines.CancellationException("Debounced job was superseded"))
    }
}
