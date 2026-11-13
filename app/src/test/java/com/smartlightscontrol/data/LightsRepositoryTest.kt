package com.smartlightscontrol.data

import com.smartlightscontrol.domain.Light
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class LightsRepositoryTest {

    @Test
    fun `toggle off updates state immediately and preserves lastBrightness`() = runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = LightsRepositoryImpl(mockService, debounceMillis = 0L, coroutineContext = coroutineContext)

        val result = repository.setPower("bed_light", false)

        assertTrue(result.isSuccess)
        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertFalse(bedLight.isOn)
        assertEquals(0, bedLight.brightness)
        assertEquals(75, bedLight.lastBrightness)
    }

    @Test
    fun `toggle on restores lastBrightness`() = runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = LightsRepositoryImpl(mockService, debounceMillis = 0L, coroutineContext = coroutineContext)

        repository.setPower("bed_light", false)
        val result = repository.setPower("bed_light", true)

        assertTrue(result.isSuccess)
        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertTrue(bedLight.isOn)
        assertEquals(75, bedLight.brightness)
    }

    @Test
    fun `non-dimmable light toggle only changes power state`() = runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = LightsRepositoryImpl(mockService, debounceMillis = 0L, coroutineContext = coroutineContext)

        val entranceInitial = repository.lights.value.first { it.id == "entrance_light" }
        assertFalse(entranceInitial.dimmable)
        assertFalse(entranceInitial.isOn)

        repository.setPower("entrance_light", true)
        val entranceOn = repository.lights.value.first { it.id == "entrance_light" }
        assertTrue(entranceOn.isOn)
        assertEquals(100, entranceOn.brightness)

        repository.setPower("entrance_light", false)
        val entranceOff = repository.lights.value.first { it.id == "entrance_light" }
        assertFalse(entranceOff.isOn)
        assertEquals(0, entranceOff.brightness)
    }

    @Test
    fun `brightness change updates optimistic state immediately`() = runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = LightsRepositoryImpl(mockService, debounceMillis = 0L, coroutineContext = coroutineContext)

        val result = repository.setBrightness("bed_light", 40)

        assertTrue(result.isSuccess)
        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertTrue(bedLight.isOn)
        assertEquals(40, bedLight.brightness)
    }

    @Test
    fun `brightness change debounces rapidly consecutive calls and sends final value`() = runTest {
        var serviceCallCount = 0
        var lastSentBrightness = -1
        val trackingService = object : LightService {
            override suspend fun list(): List<Light> = emptyList()
            override suspend fun get(id: String): Light = Light("bed_light", "Bed light", true, true, 75, 75)
            override suspend fun setPower(id: String, isOn: Boolean): Light = get(id)
            override suspend fun setBrightness(id: String, value: Int): Light {
                serviceCallCount++
                lastSentBrightness = value
                return Light(id, "Bed light", true, true, value, value)
            }
        }

        val repository = LightsRepositoryImpl(
            service = trackingService,
            debounceMillis = 300L,
            coroutineContext = coroutineContext
        )

        // Rapid brightness changes
        val job1 = async { repository.setBrightness("bed_light", 20) }
        advanceTimeBy(100.milliseconds)
        val job2 = async { repository.setBrightness("bed_light", 50) }
        advanceTimeBy(100.milliseconds)
        val job3 = async { repository.setBrightness("bed_light", 80) }

        // Advance time beyond debounce window
        advanceTimeBy(400.milliseconds)

        job1.await()
        job2.await()
        job3.await()

        // Verify only 1 network call was executed with the final value
        assertEquals(1, serviceCallCount)
        assertEquals(80, lastSentBrightness)

        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertEquals(80, bedLight.brightness)
    }

    @Test
    fun `rollback on failure restores last confirmed state`() = runTest {
        val failingService = object : LightService {
            override suspend fun list(): List<Light> = emptyList()
            override suspend fun get(id: String): Light = Light("bed_light", "Bed light", true, true, 75, 75)
            override suspend fun setPower(id: String, isOn: Boolean): Light {
                throw RuntimeException("Network error")
            }
            override suspend fun setBrightness(id: String, value: Int): Light = throw RuntimeException("Network error")
        }

        val repository = LightsRepositoryImpl(failingService, debounceMillis = 0L, coroutineContext = coroutineContext)

        val result = repository.setPower("bed_light", false)

        assertTrue(result.isFailure)
        // Verify state rolled back to confirmed state (isOn = true, brightness = 75)
        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertTrue(bedLight.isOn)
        assertEquals(75, bedLight.brightness)
    }

    @Test
    fun `rollback with two overlapping actions only rolls back failing light`() = runTest {
        val selectiveService = object : LightService {
            override suspend fun list(): List<Light> = emptyList()
            override suspend fun get(id: String): Light = Light(id, id, true, true, 75, 75)
            override suspend fun setPower(id: String, isOn: Boolean): Light {
                if (id == "bed_light") {
                    throw RuntimeException("Bed light network error")
                } else {
                    return Light(id, "Entrance light", false, isOn, if (isOn) 100 else 0, 100)
                }
            }
            override suspend fun setBrightness(id: String, value: Int): Light = throw NotImplementedError()
        }

        val repository = LightsRepositoryImpl(selectiveService, debounceMillis = 0L, coroutineContext = coroutineContext)

        // Concurrent/overlapping actions
        val action1 = async { repository.setPower("bed_light", false) }
        val action2 = async { repository.setPower("entrance_light", true) }

        val res1 = action1.await()
        val res2 = action2.await()

        assertTrue(res1.isFailure)
        assertTrue(res2.isSuccess)

        // bed_light should roll back to initial confirmed (ON)
        val bedLight = repository.lights.value.first { it.id == "bed_light" }
        assertTrue(bedLight.isOn)

        // entrance_light should stay confirmed ON
        val entranceLight = repository.lights.value.first { it.id == "entrance_light" }
        assertTrue(entranceLight.isOn)
    }
}
