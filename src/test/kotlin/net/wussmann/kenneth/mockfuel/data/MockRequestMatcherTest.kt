package net.wussmann.kenneth.mockfuel.data

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MockRequestMatcherTest {

    @Test
    fun `Should create string of ByteArray body`() {
        assertEquals("""{ "test": "abc" }""", exampleMockrequestMatcher.body())
    }

    @Test
    fun `Should return null of ByteArray body when no body set`() {
        assertEquals(null, MockRequestMatcher().body())
    }

    @ParameterizedTest
    @MethodSource("createMockRequestMatcher")
    fun `Should match fuel request`(mockRequestMatcher: MockRequestMatcher) {
        assertTrue(mockRequestMatcher.matches(exampleFuelRequest))
    }

    @ParameterizedTest
    @MethodSource("createMockRequestMatcher")
    fun `Should not match fuel request`(mockRequestMatcher: MockRequestMatcher) {
        assertFalse(mockRequestMatcher.matches(
            Fuel.delete("http://fake2.local/test123")
        ))
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

    companion object {
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

        @JvmStatic
        @Suppress("unused")
        fun createMockRequestMatcher() = listOf(
            exampleMockrequestMatcher,
            MockRequestMatcher(
                method = Method.POST
            ),
            MockRequestMatcher(
                queryParams = listOf("abc" to "123")
            ),
            MockRequestMatcher(
                headers = Headers()
                    .append("Example", "Hello")
                    .append("Content-Type", "application/json")
            ),
            MockRequestMatcher(
                host = "fake.local"
            ),
            MockRequestMatcher(
                path = "/test"
            ),
            MockRequestMatcher(
                body = """{ "test": "abc" }""".toByteArray()
            )
        )
    }
}
