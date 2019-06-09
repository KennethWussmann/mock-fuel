package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import net.wussmann.kenneth.mockfuel.data.MockResponse
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertFails

internal class MockFuelStoreTest {

    private val mockFuelStore = MockFuelStore()

    @BeforeEach
    internal fun setUp() {
        FuelManager.instance.basePath = "http://fake.local"
        mockFuelStore.reset()
    }

    @Test
    fun `Should find enqueued response`() {
        mockFuelStore enqueue MockResponse(statusCode = 200)

        val response = mockFuelStore.findResponse(Fuel.post("/test"))

        assertEquals(200, response.statusCode)
        assertEquals(0, mockFuelStore.responseQueue.size)
    }

    @Test
    fun `Should find enqueued responses in correct order`() {
        mockFuelStore.enqueue(MockResponse(statusCode = 200))
        mockFuelStore.enqueue(MockResponse(statusCode = 201))
        mockFuelStore.enqueue(MockResponse(statusCode = 404))

        val response1 = mockFuelStore.findResponse(Fuel.post("/test"))
        val response2 = mockFuelStore.findResponse(Fuel.post("/test"))
        val response3 = mockFuelStore.findResponse(Fuel.post("/test"))

        assertEquals(200, response1.statusCode)
        assertEquals(201, response2.statusCode)
        assertEquals(404, response3.statusCode)
        assertEquals(0, mockFuelStore.responseQueue.size)
    }

    @Test
    fun `Should find response for matching request`() {
        mockFuelStore.on(method = Method.POST, path = "/test") {
            MockResponse(statusCode = 201)
        }

        val response = mockFuelStore.findResponse(Fuel.post("/test"))

        assertEquals(201, response.statusCode)
        assertEquals(1, mockFuelStore.requestResponseMap.size) // specific request matchers are not cleared after serving
    }

    @Test
    fun `Should find response for matching request when multiple dispatchers registered`() {
        mockFuelStore.on(Method.POST, "/1") { MockResponse(statusCode = 201) }
        mockFuelStore.on(Method.GET, "/2") { MockResponse(statusCode = 200) }
        mockFuelStore.on(Method.DELETE, "/3") { MockResponse(statusCode = 204) }

        val response1 = mockFuelStore.findResponse(Fuel.post("/1"))
        val response2 = mockFuelStore.findResponse(Fuel.get("/2"))
        val response3 = mockFuelStore.findResponse(Fuel.delete("/3"))

        assertEquals(201, response1.statusCode)
        assertEquals(200, response2.statusCode)
        assertEquals(204, response3.statusCode)
    }

    @Test
    fun `Should find default response when no response found`() {
        mockFuelStore.on(method = Method.POST, path = "/test") {
            MockResponse(statusCode = 201)
        }

        val response = mockFuelStore.findResponse(Fuel.get("/abc"))

        assertEquals(404, response.statusCode)
    }

    @Test
    fun `Should find any matching response`() {
        mockFuelStore.on {
            MockResponse(statusCode = 200)
        }

        val response = mockFuelStore.findResponse(Fuel.get("/abc"))

        assertEquals(200, response.statusCode)
    }

    @Test
    fun `Should find overriden default response when no response found`() {
        mockFuelStore.defaultResponse = MockResponse(statusCode = 500)
        mockFuelStore.on(method = Method.POST, path = "/test") {
            MockResponse(statusCode = 201)
        }

        val response = mockFuelStore.findResponse(Fuel.get("/abc"))

        assertEquals(500, response.statusCode)
    }

    @Test
    fun `Should find response for matching request, then enqueued and then default response`() {
        mockFuelStore.defaultResponse = MockResponse(statusCode = 500)
        mockFuelStore.on(Method.POST, "/2") { MockResponse(statusCode = 201) }
        mockFuelStore.enqueue(MockResponse(statusCode = 502, body = "Bad Gateway".toByteArray()))

        val response1 = mockFuelStore.findResponse(Fuel.post("/2"))
        val response2 = mockFuelStore.findResponse(Fuel.get("/from-queue"))
        val response3 = mockFuelStore.findResponse(Fuel.delete("/default"))

        assertEquals(201, response1.statusCode)
        assertEquals(502, response2.statusCode)
        assertEquals("Bad Gateway", response2.body())
        assertEquals(500, response3.statusCode)
    }

    @Test
    fun `Should add response to queue when enqueued`() {
        mockFuelStore.enqueue(MockResponse(statusCode = 200))

        assertEquals(1, mockFuelStore.responseQueue.size)
        assertEquals(200, mockFuelStore.responseQueue.first().statusCode)
    }

    @Test
    fun `Should reset everything of MockFuelStore`() {
        mockFuelStore.defaultResponse = MockResponse(statusCode = 500)
        mockFuelStore.on(Method.POST, "/2") { MockResponse(statusCode = 201) }
        mockFuelStore.enqueue(MockResponse(statusCode = 502, body = "Bad Gateway".toByteArray()))
        mockFuelStore.recordedRequests.add(MockRequestMatcher())

        mockFuelStore.reset()

        assertEquals(0, mockFuelStore.responseQueue.size)
        assertEquals(0, mockFuelStore.recordedRequests.size)
        assertEquals(0, mockFuelStore.requestResponseMap.size)
        assertEquals(404, mockFuelStore.defaultResponse.statusCode)
    }

    @Test
    fun `Should create MockRequestMatcher and put to MockFuelStore`() {
        mockFuelStore.on(
                Method.DELETE,
                "/something",
                "fake.local",
                "Something".toByteArray(),
                Headers().append("Example", "Test"),
                listOf("abc" to "123")
        ) {
            MockResponse(statusCode = 200)
        }

        assertEquals(1, mockFuelStore.requestResponseMap.size)

        val requestResponse = mockFuelStore.requestResponseMap.entries.first()

        assertEquals(Method.DELETE, requestResponse.key.method)
        assertEquals("/something", requestResponse.key.path)
        assertEquals("fake.local", requestResponse.key.host)
        assertTrue("Something".toByteArray().contentEquals(requestResponse.key.body!!))
        assertEquals(mapOf("Example" to listOf("Test")), requestResponse.key.headers)
        assertEquals(listOf("abc" to "123"), requestResponse.key.queryParams)

        assertEquals(200, requestResponse.value.statusCode)
    }

    @Test
    fun `Should take first recorded request and verify it`() {
        mockFuelStore.recordedRequests.add(
                MockRequestMatcher(
                        method = Method.POST,
                        queryParams = listOf("abc" to "123"),
                        headers = Headers()
                                .append("Example", "Hello")
                                .append("Content-Type", "application/json"),
                        host = "fake.local",
                        path = "/test",
                        body = """{ "test": "abc" }""".toByteArray()
                )
        )

        mockFuelStore.verifyRequest {
            assertMethod(Method.POST)
            assertQueryParam("abc", "123")
            assertHeader("Example", "Hello")
            assertHeader("Content-Type", "application/json")
            assertHost("fake.local")
            assertPath("/test")
            assertBody("""{ "test": "abc" }""")
        }
    }

    @Test
    fun `Should assert any recorded request was found`() {
        mockFuelStore.recordedRequests.add(
                MockRequestMatcher(
                        method = Method.POST,
                        queryParams = listOf("abc" to "123"),
                        headers = Headers()
                                .append("Example", "Hello")
                                .append("Content-Type", "application/json"),
                        host = "fake.local",
                        path = "/test",
                        body = """{ "test": "abc" }""".toByteArray()
                )
        )

        mockFuelStore.verifyRequest()
    }

    @Test
    fun `Should fail when no recorded request found`() {
        assertFails {
            mockFuelStore.verifyRequest()
        }
    }
}