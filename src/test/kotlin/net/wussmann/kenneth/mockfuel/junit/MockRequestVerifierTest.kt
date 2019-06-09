package net.wussmann.kenneth.mockfuel.junit

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import org.junit.jupiter.api.Test

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

    private fun withInstance(task: MockRequestVerifier.() -> Unit) = instance.task()

    @Test
    fun `Should assert any body is present`() {
        instance.assertAnyBody()
    }

    @Test
    fun `Should assert body equals ByteArray`() {
        withInstance {
            assertBody("""{ "test": "abc" }""".toByteArray())
            request body """{ "test": "abc" }""".toByteArray()
        }
    }

    @Test
    fun `Should assert body equals String`() {
        withInstance {
            assertBody("""{ "test": "abc" }""")
            request body """{ "test": "abc" }"""
        }
    }

    @Test
    fun `Should assert request method is POST`() {
        withInstance {
            assertMethod(Method.POST)
            request method Method.POST
        }
    }

    @Test
    fun `Should assert host equals`() {
        withInstance {
            assertHost("fake.local")
            request host "fake.local"
        }
    }

    @Test
    fun `Should assert path equals`() {
        withInstance {
            assertPath("/test")
            request host "/test"
        }
    }

    @Test
    fun `Should assert any query param is present`() {
        instance.assertAnyQueryParam()
    }

    @Test
    fun `Should assert query param with given key is present no matter of its value`() {
        withInstance {
            assertQueryParam("abc")
            request queryParam "abc"
        }
    }

    @Test
    fun `Should assert query param has given value`() {
        withInstance {
            assertQueryParam("abc", "123")
            assertQueryParam("abc", "456")

            request queryParam "abc" to "123"
            request queryParam "abc" to "456"
        }
    }

    @Test
    fun `Should assert query param has all given values`() {
        withInstance {
            assertQueryParams("abc", listOf("123", "456"))
            request queryParams ("abc" to listOf("123", "456"))
        }
    }

    @Test
    fun `Should assert all query params exactly match given value`() {
        withInstance {
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
        withInstance {
            assertHeader("Example")
            request header "Example"
        }
    }

    @Test
    fun `Should assert header exists with given value`() {
        withInstance {
            assertHeader("Example", "Hello")
            assertHeader("Example", "World")

            request header "Example" to "Hello"
            request header "Example" to "World"
        }
    }

    @Test
    fun `Should assert header has all given values`() {
        withInstance {
            assertHeaders("Example", listOf("Hello", "World"))
            request headers ("Example" to listOf("Hello", "World"))
        }
    }

    @Test
    fun `Should assert all headers exactly match given value`() {
        withInstance {
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