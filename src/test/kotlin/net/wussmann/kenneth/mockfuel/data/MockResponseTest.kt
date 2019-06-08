package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.core.BodyLength
import com.github.kittinunf.fuel.core.BodySource
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

internal class MockResponseTest {

    private val exampleUrl = URL("http://fake.local/test")

    private val exampleMockResponse = MockResponse(
            statusCode = 200,
            body = "Hello World".toByteArray(),
            headers = Headers().append("Example", "Hello")
    )

    private val exampleFuelResponse = Response(
            url = exampleUrl,
            body = let {
                val body = "Hello World".toByteArray()
                val bodySource: BodySource = { body.inputStream() }
                val bodyLength: BodyLength = { body.size.toLong() }
                DefaultBody.from(bodySource, bodyLength, Charsets.UTF_8)
            },
            headers = Headers().append("Example", "Hello"),
            statusCode = 200
    )

    @Test
    fun `Should create string of ByteArray body`() {
        assertEquals("Hello World", exampleMockResponse.body())
        assertEquals(Headers().append("Example", "Hello").toString(), exampleMockResponse.headers.toString())
    }

    @Test
    fun `Should return null when ByteArray body is null`() {
        assertEquals(null, MockResponse(statusCode = 200).body())
    }

    @Test
    fun `Should map MockResponse to Fuel Response`() {
        assertEquals(exampleFuelResponse.toString(), exampleMockResponse.toResponse(exampleUrl).toString())
    }
}