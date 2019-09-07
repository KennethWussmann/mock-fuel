package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import net.wussmann.kenneth.mockfuel.data.MockResponse
import net.wussmann.kenneth.mockfuel.junit.MockRequestVerifier
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Store for Fuel responses to requests
 */
class MockFuelStore {

    val requestResponseMap: MutableMap<MockRequestMatcher, MockResponse> = mutableMapOf()
    val responseQueue: MutableList<MockResponse> = mutableListOf()
    val recordedRequests: MutableList<MockRequestMatcher> = mutableListOf()
    var defaultResponse: MockResponse = createDefaultMockResponse()

    private fun takeFirst() = responseQueue.firstOrNull()?.also { responseQueue.removeAt(0) }

    private fun takeFirstRecorded() = recordedRequests.firstOrNull()?.also { recordedRequests.removeAt(0) }

    private fun createDefaultMockResponse(): MockResponse = MockResponse(
        headers = Headers(),
        statusCode = 404
    )

    /**
     * Find a [MockResponse] for a given Fuel [Request]
     */
    fun findResponse(request: Request): MockResponse = requestResponseMap.entries
        .firstOrNull { (key, _) -> key.matches(request) }
        ?.value
        ?: takeFirst()
        ?: defaultResponse

    /**
     * Add the response to the queue.
     * It will be served as response when there is a request and no other response was found to match.
     */
    fun enqueue(mockResponse: MockResponse) = responseQueue.add(mockResponse)

    /**
     * Reset the response queue, matching responses, recorded requests and the default response
     */
    fun reset() {
        responseQueue.clear()
        recordedRequests.clear()
        requestResponseMap.clear()
        defaultResponse = createDefaultMockResponse()
    }

    /**
     * When a request is made that matches here given attributes it will respond with the given [MockResponse]
     */
    fun on(
        method: Method? = null,
        path: String? = null,
        host: String? = null,
        body: ByteArray? = null,
        headers: Headers? = null,
        queryParams: Parameters? = null,
        answer: () -> MockResponse
    ) {
        val matcher = MockRequestMatcher(
            method,
            path,
            host,
            body,
            headers,
            queryParams
        )
        val response = answer()
        requestResponseMap[matcher] = response
    }

    /**
     * Verify the next recorded request to match given attributes.
     * Even without a given task the method will assert that there was a request.
     * @param task optional task that contains assertions
     */
    fun verifyRequest(task: (MockRequestVerifier.() -> Unit)? = null) {
        val record = takeFirstRecorded()
        assertNotNull(record, "Expected request was made but none found.")
        task?.invoke(MockRequestVerifier(record!!))
    }

    fun droppingTheCoverage() {
        println("Drop the coverage!")
    }
}

/**
 * Infix for enqueuing [MockResponse]s to the [MockFuelStore]
 */
infix fun MockFuelStore.enqueue(mockResponse: MockResponse) = this.enqueue(mockResponse)

/**
 * Infix for verifying the next request in queue
 */
infix fun MockFuelStore.verify(task: (MockRequestVerifier.() -> Unit)?) = this.verifyRequest(task)

/**
 * Function to indicate any request should be present
 */
fun any() = null