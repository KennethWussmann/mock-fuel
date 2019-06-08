package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import net.wussmann.kenneth.mockfuel.data.MockResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class MockFuelClientTest {

    private val mockFuelStore = MockFuelStore()
    private val instance = spyk(MockFuelClient(mockFuelStore))

    @BeforeEach
    internal fun setUp() {
        FuelManager.instance.basePath = "http://fake.local"
    }

    @Test
    fun `Should find response from store`() {
        mockFuelStore.enqueue(MockResponse(statusCode = 200))

        val response = instance.executeRequest(Fuel.post("/test"))

        assertEquals(200, response.statusCode)
        assertEquals(0, mockFuelStore.responseQueue.size)
    }

    @Test
    fun `Should put request to recorded requests`() {
        instance.executeRequest(Fuel.post("/test"))

        assertEquals(1, mockFuelStore.recordedRequests.size)
    }

    @ParameterizedTest
    @ValueSource(longs = [50, 500, 1000, 2000])
    fun `Should delay response when set`(delay: Long) {
        val startedAt = System.currentTimeMillis()
        mockFuelStore.enqueue(
                MockResponse(
                        statusCode = 200,
                        delay = delay
                )
        )

        instance.executeRequest(Fuel.post("/test"))
        val rtt = System.currentTimeMillis() - startedAt

        // assert round-trip-time to at least be as long as we delayed it
        assertTrue(rtt >= delay)
    }

    @Test
    fun `Should invoke executeRequest when awaitRequest is called`() {
        runBlocking {
            instance.awaitRequest(Fuel.post("/test"))
        }
        verify {
            instance.executeRequest(any())
        }
    }
}