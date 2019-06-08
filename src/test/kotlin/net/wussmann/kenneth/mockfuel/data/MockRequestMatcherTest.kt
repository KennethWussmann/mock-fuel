package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class MockRequestMatcherTest {

    private val exampleMockrequestMatcher = MockRequestMatcher(
            method = Method.POST,
            queryParams = listOf("abc" to "123"),
            headers = Headers()
                    .append("Example", "Hello")
                    .append("Content-Type", "application/json"),
            host = "fake.local",
            path = "/test",
            body = """{ "test": "abc" }""".toByteArray()
    )
    private val exampleFuelRequest = Fuel
            .post("http://fake.local/test", listOf("abc" to "123"))
            .header("Example", "Hello")
            .jsonBody("""{ "test": "abc" }""")
            .request

    @Test
    fun `Should match fuel request`() {
        assertTrue(exampleMockrequestMatcher.matches(exampleFuelRequest))
    }

    @Test
    fun `Should match example after fuel request was mapped`() {
        val actual = MockRequestMatcher.from(exampleFuelRequest)
        assertEquals(exampleMockrequestMatcher.toString(), actual.toString())
    }

    @Test
    fun `Should match example after fuel request was mapped with query params in url`() {
        val actual = MockRequestMatcher.from(
                Fuel
                        .post("http://fake.local/test?abc=123")
                        .header("Example", "Hello")
                        .jsonBody("""{ "test": "abc" }""")
                        .request
        )
        assertEquals(exampleMockrequestMatcher.toString(), actual.toString())
    }

    @Test
    fun `Should map empty query params in url`() {
        val actual = MockRequestMatcher.from(
                Fuel.post("http://fake.local/test?")
        )
        assertEquals(listOf<Pair<String, Any?>>(), actual.queryParams)
    }
}