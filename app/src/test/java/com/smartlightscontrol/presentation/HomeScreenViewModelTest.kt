package com.smartlightscontrol.presentation

import com.smartlightscontrol.data.LightService
import com.smartlightscontrol.data.LightsRepositoryImpl
import com.smartlightscontrol.data.MockLightService
import com.smartlightscontrol.domain.Light
import com.smartlightscontrol.domain.LightsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(service: LightService): LightsRepository {
        return LightsRepositoryImpl(
            service = service,
            debounceMillis = 0L,
            coroutineContext = testDispatcher
        )
    }

    @Test
    fun `initial state collects lights and selects default light`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository)

        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertEquals(3, state.lights.size)
        assertEquals("bed_light", state.selectedLightId)
        assertNotNull(state.selectedLight)
        assertEquals("Bed light", state.selectedLight?.name)
        assertFalse(state.isDrawerOpen)
        assertFalse(state.hasError)
    }

    @Test
    fun `SelectLight event updates selectedLightId`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.SelectLight("entrance_light"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertEquals("entrance_light", state.selectedLightId)
        assertEquals("Entrance light", state.selectedLight?.name)
    }

    @Test
    fun `TogglePower success updates light power state`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.TogglePower("bed_light"))
        testDispatcher.scheduler.runCurrent()

        val bedLight = viewModel.state.value.lights.first { it.id == "bed_light" }
        assertFalse(bedLight.isOn)
        assertFalse(viewModel.state.value.hasError)
    }

    @Test
    fun `TogglePower failure from widget sets widgetError to true`() = testScope.runTest {
        val failingService = object : LightService {
            override suspend fun list(): List<Light> = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L }).list()
            override suspend fun get(id: String): Light = throw RuntimeException("Error")
            override suspend fun setPower(id: String, isOn: Boolean): Light = throw RuntimeException("Error")
            override suspend fun setBrightness(id: String, value: Int): Light = throw RuntimeException("Error")
        }
        val repository = createRepository(failingService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        // Toggle from widget (drawer closed)
        viewModel.state.value.eventTunnel(HomeScreenEvent.TogglePower("bed_light"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertTrue(state.widgetError)
        assertFalse(state.drawerError)
        assertEquals("Something went wrong · Try again later", state.errorMessage)
    }

    @Test
    fun `TogglePower failure from drawer sets drawerError to true`() = testScope.runTest {
        val failingService = object : LightService {
            override suspend fun list(): List<Light> = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L }).list()
            override suspend fun get(id: String): Light = throw RuntimeException("Error")
            override suspend fun setPower(id: String, isOn: Boolean): Light = throw RuntimeException("Error")
            override suspend fun setBrightness(id: String, value: Int): Light = throw RuntimeException("Error")
        }
        val repository = createRepository(failingService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        // Open drawer for bed_light
        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("bed_light"))
        testDispatcher.scheduler.runCurrent()

        // Toggle power for drawer light while drawer is open
        viewModel.state.value.eventTunnel(HomeScreenEvent.TogglePower("bed_light"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertFalse(state.widgetError)
        assertTrue(state.drawerError)
        assertEquals("Something went wrong · Try again later", state.errorMessage)
    }

    @Test
    fun `ChangeBrightness failure from drawer sets drawerError`() = testScope.runTest {
        val failingService = object : LightService {
            override suspend fun list(): List<Light> = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L }).list()
            override suspend fun get(id: String): Light = throw RuntimeException("Error")
            override suspend fun setPower(id: String, isOn: Boolean): Light = throw RuntimeException("Error")
            override suspend fun setBrightness(id: String, value: Int): Light = throw RuntimeException("Error")
        }
        val repository = createRepository(failingService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("bed_light"))
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.ChangeBrightness("bed_light", 50))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertFalse(state.widgetError)
        assertTrue(state.drawerError)
    }

    @Test
    fun `OpenDrawer opens drawer and sets drawerLightId`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("bed_light"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertTrue(state.isDrawerOpen)
        assertEquals("bed_light", state.drawerLightId)
        assertNotNull(state.drawerLight)
        assertEquals("Bed light", state.drawerLight?.name)
    }

    @Test
    fun `OpenDrawer with invalid id triggers widget error`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("unknown_id"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isDrawerOpen)
        assertTrue(state.widgetError)
    }

    @Test
    fun `CloseDrawer closes drawer and clears drawerLightId`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("bed_light"))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.state.value.isDrawerOpen)

        viewModel.state.value.eventTunnel(HomeScreenEvent.CloseDrawer)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isDrawerOpen)
        assertNull(state.drawerLightId)
        assertNull(state.drawerLight)
    }

    @Test
    fun `DismissError clears error state`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        // Trigger an error by opening invalid light
        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("invalid_id"))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.state.value.hasError)

        // Dismiss error
        viewModel.state.value.eventTunnel(HomeScreenEvent.DismissError)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assertFalse(state.widgetError)
        assertFalse(state.drawerError)
        assertNull(state.errorMessage)
    }

    @Test
    fun `subsequent user action automatically dismisses active error toast`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 4000L)
        testDispatcher.scheduler.runCurrent()

        // Trigger an error
        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("invalid_id"))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.state.value.hasError)

        // Perform a new user action (SelectLight)
        viewModel.state.value.eventTunnel(HomeScreenEvent.SelectLight("entrance_light"))
        testDispatcher.scheduler.runCurrent()

        // Verify active error was automatically dismissed
        val state = viewModel.state.value
        assertFalse(state.hasError)
        assertFalse(state.widgetError)
        assertNull(state.errorMessage)
        assertEquals("entrance_light", state.selectedLightId)
    }

    @Test
    fun `auto dismiss error clears error after specified delay`() = testScope.runTest {
        val mockService = MockLightService(failureRate = 0.0, delayMillisProvider = { 0L })
        val repository = createRepository(mockService)
        val viewModel = HomeScreenViewModel(repository, autoDismissDelayMs = 2000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.state.value.eventTunnel(HomeScreenEvent.OpenDrawer("invalid_id"))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.state.value.hasError)

        advanceTimeBy(1000.milliseconds)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.state.value.hasError)

        advanceTimeBy(1100.milliseconds)
        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.state.value.hasError)
    }
}
