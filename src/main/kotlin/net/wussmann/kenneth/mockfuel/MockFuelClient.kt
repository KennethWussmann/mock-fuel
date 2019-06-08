package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher

/**
 * Implementation of the Fuel http client that doesn't emit requests to any external services
 */
class MockFuelClient(private val mockFuelStore: MockFuelStore) : Client {

    override fun executeRequest(request: Request): Response {
        val response = mockFuelStore.findResponse(request)
        mockFuelStore.recordedRequests.add(MockRequestMatcher.from(request))
        return if (response.delay > 0) {
            runBlocking {
                delay(response.delay)
                response.toResponse(request.url)
            }
        } else response.toResponse(request.url)
    }
}