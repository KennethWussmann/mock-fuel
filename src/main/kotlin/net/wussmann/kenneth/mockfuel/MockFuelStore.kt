package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import net.wussmann.kenneth.mockfuel.data.AbstractResponse
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import net.wussmann.kenneth.mockfuel.data.MockResponse
import net.wussmann.kenneth.mockfuel.junit.MockRequestVerifier
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Store for Fuel responses to requests
 */
class MockFuelStore(
    val requestResponseMap: MutableMap<MockRequestMatcher, AbstractResponse> = mutableMapOf(),
    val responseQueue: MutableList<AbstractResponse> = mutableListOf(),
    val recordedRequests: MutableList<MockRequestMatcher> = mutableListOf(),
    val passThroughClient: Client = FuelManager.instance.client
) {
    var defaultResponse: AbstractResponse = createDefaultMockResponse()

    private fun takeFirst() = responseQueue.firstOrNull()?.also { responseQueue.removeAt(0) }

    private fun takeFirstRecorded() = recordedRequests.firstOrNull()?.also { recordedRequests.removeAt(0) }

    private fun createDefaultMockResponse(): MockResponse = MockResponse(
        headers = Headers(),
        statusCode = 404
    )

    /**
     * Find an [AbstractResponse] for a given Fuel [Request]
     */
    fun findResponse(request: Request): AbstractResponse = requestResponseMap.entries
        .firstOrNull { (key, _) -> key.matches(request) }
        ?.value
        ?: takeFirst()
        ?: defaultResponse

    /**
     * Add the response to the queue.
     * It will be served as response when there is a request and no other response was found to match.
     */
    fun enqueue(response: AbstractResponse) = responseQueue.add(response)

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
        answer: () -> AbstractResponse
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
}

/**
 * Infix for enqueuing [AbstractResponse]s to the [MockFuelStore]
 */
infix fun MockFuelStore.enqueue(abstractResponse: AbstractResponse) = this.enqueue(abstractResponse)

/**
 * Infix for verifying the next request in queue
 */
infix fun MockFuelStore.verify(task: (MockRequestVerifier.() -> Unit)?) = this.verifyRequest(task)

/**
 * Function to indicate any request should be present
 */
fun any() = null
