package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request

/**
 * Representation for a request to match it against Fuel request
 */
data class MockRequestMatcher(
    val method: Method? = null,
    val path: String? = null,
    val host: String? = null,
    val body: ByteArray? = null,
    val headers: Headers? = null,
    val queryParams: Parameters? = null
) {
    /**
     * Get the body as string
     */
    fun body(): String? = body?.let { String(it) }

    /**
     * Check if the given Fuel [Request] is matching this [MockRequestMatcher]
     */
    fun matches(request: Request): Boolean {
        return when {
            method != null && method != request.method -> false
            path != null && request.url.path != path -> false
            host != null && request.url.host != host -> false
            body != null && !request.body.toByteArray().contentEquals(body) -> false
            headers != null && !request.headers.entries.containsAll(headers.entries) -> false
            queryParams != null && request.parameters != queryParams -> false
            else -> true
        }
    }

    companion object {
        /**
         * Map a Fuel [Request] to a [MockRequestMatcher]
         */
        fun from(request: Request): MockRequestMatcher {
            return MockRequestMatcher(
                    method = request.method,
                    body = request.body.toByteArray(),
                    path = request.url.path,
                    host = request.url.host,
                    headers = request.headers,
                    queryParams = mapQueryParams(request.url.query) ?: request.parameters
            )
        }

        private fun mapQueryParams(queryString: String?): Parameters? {
            if (queryString == null) {
                return null
            }

            return queryString.split("&").mapNotNull {
                val pair = it.split("=")
                if (pair.first() == "") {
                    null
                } else {
                    pair.first() to pair.getOrNull(1)
                }
            }
        }
    }
}
