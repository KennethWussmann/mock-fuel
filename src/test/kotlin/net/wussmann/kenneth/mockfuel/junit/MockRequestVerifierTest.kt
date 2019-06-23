package net.wussmann.kenneth.mockfuel.junit

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class MockRequestVerifierTest {

    private val instance = MockRequestVerifier(
            MockRequestMatcher(
                    method = Method.POST,
                    queryParams = listOf("abc" to "123", "abc" to "456", "def" to "789"),
                    headers = Headers()
                            .append("Example", listOf("Hello", "World"))
                            .append("Content-Type", "application/json"),
                    host = "fake.local",
                    path = "/test",
                    body = """{ "test": "abc" }""".toByteArray()
            )
    )

    @Test
    fun `Should assert any body is present`() {
        instance.assertAnyBody()
    }

    @Test
    fun `Should assert body equals ByteArray`() {
        with(instance) {
            assertBody("""{ "test": "abc" }""".toByteArray())
            request body """{ "test": "abc" }""".toByteArray()
        }
    }

    @Test
    fun `Should assert body equals String`() {
        with(instance) {
            assertBody("""{ "test": "abc" }""")
            request body """{ "test": "abc" }"""
        }
    }

    @Test
    fun `Should assert request method is POST`() {
        with(instance) {
            assertMethod(Method.POST)
            request method Method.POST
        }
    }

    @Test
    fun `Should assert host equals`() {
        with(instance) {
            assertHost("fake.local")
            request host "fake.local"
        }
    }

    @Test
    fun `Should assert path equals`() {
        with(instance) {
            assertPath("/test")
            request path "/test"
        }
    }

    @Test
    fun `Should assert any query param is present`() {
        instance.assertAnyQueryParam()
    }

    @Test
    fun `Should assert query param with given key is present no matter of its value`() {
        with(instance) {
            assertQueryParam("abc")
            request queryParam "abc"
        }
        with(MockRequestVerifier(MockRequestMatcher(
            queryParams = listOf("abc" to null, "def" to "")
        ))) {
            assertQueryParam("abc")
            assertQueryParam("def")
            request queryParam "abc"
        }
    }

    @Test
    fun `Should assert query param with given key is not present`() {
        with(instance) {
            assertFails {
                assertQueryParam("test")
            }
            assertFails {
                request queryParam "test"
            }
        }
    }

    @Test
    fun `Should assert query param with given key is not present when query params are null`() {
        with(MockRequestVerifier(MockRequestMatcher())) {
            assertFails {
                assertQueryParam("abc")
            }
            assertFails {
                assertQueryParam("def")
            }
        }
    }

    @Test
    fun `Should assert query param has given value`() {
        with(instance) {
            assertQueryParam("abc", "123")
            assertQueryParam("abc", "456")

            request queryParam "abc" eq "123"
            request queryParam "abc" eq "456"

            assertFails {
                request queryParam "abc" eq "fail!"
            }
        }
    }

    @Test
    fun `Should assert query param has all given values`() {
        with(instance) {
            assertQueryParams("abc", listOf("123", "456"))
            request queryParams "abc" eq listOf("123", "456")
        }
    }

    @Test
    fun `Should assert all query params exactly match given value`() {
        with(instance) {
            assertQueryParams(listOf(
                "abc" to "123", "abc" to "456", "def" to "789"
            ))
            request queryParams listOf(
                "abc" to "123", "abc" to "456", "def" to "789"
            )
        }
    }

    @Test
    fun `Should assert any header is present`() {
        instance.assertAnyHeader()
    }

    @Test
    fun `Should assert header with given key is present no matter of its value`() {
        with(instance) {
            assertHeader("Example")
            request header "Example"
        }
    }

    @Test
    fun `Should assert header exists with given value`() {
        with(instance) {
            assertHeader("Example", "Hello")
            assertHeader("Example", "World")

            request header "Example" eq "Hello"
            request header "Example" eq "World"
        }
    }

    @Test
    fun `Should assert header has all given values`() {
        with(instance) {
            assertHeaders("Example", listOf("Hello", "World"))
            request headers "Example" eq listOf("Hello", "World")
        }
    }

    @Test
    fun `Should assert all headers exactly match given value`() {
        with(instance) {
            assertHeaders(
                Headers()
                    .append("Example", listOf("Hello", "World"))
                    .append("Content-Type", "application/json")
            )
            request headers Headers()
                .append("Example", listOf("Hello", "World"))
                .append("Content-Type", "application/json")
        }
    }
}