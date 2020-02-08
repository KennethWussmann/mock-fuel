package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.core.BodyLength
import com.github.kittinunf.fuel.core.BodySource
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import java.net.URL

abstract class AbstractResponse(
    @JvmField
    open val statusCode: Int = 200,
    @JvmField
    open val body: ByteArray? = null,
    @JvmField
    open val headers: Headers = Headers(),
    /**
     * Put some artificial delay to the response to simulate round-trip-time
     */
    open val delay: Long = 0
) {
    /**
     * Get the body as string
     */
    fun body(): String? = body?.let { String(it) }

    /**
     * Map the [MockResponse] to a Fuel [Response]
     */
    fun toResponse(url: URL) = Response(
        url = url,
        statusCode = statusCode,
        headers = headers,
        body = if (body != null) {
            val bodySource: BodySource = { body!!.inputStream() }
            val bodyLength: BodyLength = { body!!.size.toLong() }
            DefaultBody.from(bodySource, bodyLength, Charsets.UTF_8)
        } else DefaultBody()
    )
}
