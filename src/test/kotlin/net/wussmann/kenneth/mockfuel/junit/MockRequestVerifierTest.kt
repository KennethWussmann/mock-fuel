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

    @Test
    fun `Should assert any body is present`() {
        instance.assertAnyBody()
    }

    @Test
    fun `Should assert body equals ByteArray`() {
        instance.assertBody("""{ "test": "abc" }""".toByteArray())
    }

    @Test
    fun `Should assert body equals String`() {
        instance.assertBody("""{ "test": "abc" }""")
    }

    @Test
    fun `Should assert request method is POST`() {
        instance.assertMethod(Method.POST)
    }

    @Test
    fun `Should assert host equals`() {
        instance.assertHost("fake.local")
    }

    @Test
    fun `Should assert path equals`() {
        instance.assertPath("/test")
    }

    @Test
    fun `Should assert any query param is present`() {
        instance.assertAnyQueryParam()
    }

    @Test
    fun `Should assert query param with given key is present no matter of its value`() {
        instance.assertQueryParam("abc")
    }

    @Test
    fun `Should assert query param has given value`() {
        instance.assertQueryParam("abc", "123")
        instance.assertQueryParam("abc", "456")
    }

    @Test
    fun `Should assert query param has all given values`() {
        instance.assertQueryParams("abc", listOf("123", "456"))
    }

    @Test
    fun `Should assert all query params exactly match given value`() {
        instance.assertQueryParams(listOf(
                "abc" to "123", "abc" to "456", "def" to "789"
        ))
    }

    @Test
    fun `Should assert any header is present`() {
        instance.assertAnyHeader()
    }

    @Test
    fun `Should assert header with given key is present no matter of its value`() {
        instance.assertHeader("Example")
    }

    @Test
    fun `Should assert header exists with given value`() {
        instance.assertHeader("Example", "Hello")
        instance.assertHeader("Example", "World")
    }

    @Test
    fun `Should assert header has all given values`() {
        instance.assertHeaders("Example", listOf("Hello", "World"))
    }

    @Test
    fun `Should assert all headers exactly match given value`() {
        instance.assertHeaders(
                Headers()
                        .append("Example", listOf("Hello", "World"))
                        .append("Content-Type", "application/json")
        )
    }
}