package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.core.Headers

/**
 * Representation of a response that should be returned
 */
data class MockResponse(
    override val statusCode: Int,
    override val body: ByteArray? = null,
    override val headers: Headers = Headers(),
    /**
     * Put some artificial delay to the response to simulate round-trip-time
     */
    override val delay: Long = 0
) : AbstractResponse(statusCode, body, headers, delay) {

    companion object {
        /**
         * Predefined [MockResponse] for a request that timed-out
         */
        fun timeout() = MockResponse(statusCode = 408)
        /**
         * Predefined [MockResponse] for a request that was OK
         */
        fun ok() = MockResponse(statusCode = 200)
    }
}
