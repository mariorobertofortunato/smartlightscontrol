package com.smartlightscontrol.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockLightServiceTest {

    private lateinit var service: MockLightService

    @Before
    fun setUp() {
        service = MockLightService(
            failureRate = 0.0,
            delayMillisProvider = { 0L }
        )
    }

    @Test
    fun `list returns initial seed lights`() = runTest {
        val lights = service.list()
        assertEquals(3, lights.size)
        assertEquals("Bed light", lights[0].name)
        assertTrue(lights[0].dimmable)
        assertTrue(lights[0].isOn)
        assertEquals(75, lights[0].brightness)
    }

    @Test
    fun `setPower false turns light off and preserves lastBrightness`() = runTest {
        val updated = service.setPower("bed_light", false)
        assertFalse(updated.isOn)
        assertEquals(0, updated.brightness)
        assertEquals(75, updated.lastBrightness)
    }

    @Test
    fun `setPower true restores lastBrightness`() = runTest {
        service.setPower("bed_light", false)
        val turnedOn = service.setPower("bed_light", true)
        assertTrue(turnedOn.isOn)
        assertEquals(75, turnedOn.brightness)
    }

    @Test
    fun `setBrightness updates brightness and turns light on`() = runTest {
        val updated = service.setBrightness("bed_light", 50)
        assertTrue(updated.isOn)
        assertEquals(50, updated.brightness)
        assertEquals(50, updated.lastBrightness)
    }

    @Test
    fun `setBrightness to 0 turns light off`() = runTest {
        val updated = service.setBrightness("bed_light", 0)
        assertFalse(updated.isOn)
        assertEquals(0, updated.brightness)
    }

    @Test(expected = Exception::class)
    fun `failureRate 1_0 throws exception on request`() = runTest {
        val failingService = MockLightService(
            failureRate = 1.0,
            delayMillisProvider = { 0L }
        )
        failingService.get("bed_light")
    }
}
