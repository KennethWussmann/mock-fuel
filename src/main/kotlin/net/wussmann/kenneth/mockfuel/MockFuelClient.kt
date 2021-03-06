package net.wussmann.kenneth.mockfuel

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import net.wussmann.kenneth.mockfuel.data.PassThroughResponse

/**
 * Implementation of the Fuel http client that doesn't emit requests to any external services
 */
class MockFuelClient(private val mockFuelStore: MockFuelStore) : Client {

    override fun executeRequest(request: Request): Response {
        val response = mockFuelStore.findResponse(request)
        mockFuelStore.recordedRequests.add(MockRequestMatcher.from(request))
        if (response.delay > 0) {
            runBlocking {
                delay(response.delay)
            }
        }
        return if (response is PassThroughResponse) {
            mockFuelStore.passThroughClient.executeRequest(request)
        } else response.toResponse(request.url)
    }
}
