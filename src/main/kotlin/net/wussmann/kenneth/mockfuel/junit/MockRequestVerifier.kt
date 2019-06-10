package net.wussmann.kenneth.mockfuel.junit

import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import net.wussmann.kenneth.mockfuel.data.MockRequestMatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Verification utility with shorthands for common assertions on a recorded [MockRequestMatcher]
 */
@Suppress("TooManyFunctions")
class MockRequestVerifier(@Suppress("MemberVisibilityCanBePrivate") val request: MockRequestMatcher) {

    fun Parameters.get(key: String): List<Any?> {
        return this.filter { it.first == key }.map { it.second }
    }

    fun assertAnyBody() = assertNotNull(request.body, "Expected request body to be present but none found.")

    infix fun MockRequestMatcher.body(expected: ByteArray) = assertBody(expected)

    fun assertBody(expected: ByteArray) {
        assertAnyBody()
        assertTrue(expected.contentEquals(request.body!!), "Expected body byte array contents to match.")
    }

    infix fun MockRequestMatcher.body(expected: String) = assertBody(expected)

    fun assertBody(expected: String) {
        assertAnyBody()
        assertEquals(expected, String(request.body!!))
    }

    infix fun MockRequestMatcher.method(expected: Method) = assertMethod(expected)

    fun assertMethod(expected: Method) = assertEquals(expected, request.method)

    infix fun MockRequestMatcher.host(expected: String) = assertHost(expected)

    fun assertHost(expected: String) = assertEquals(expected, request.host)

    infix fun MockRequestMatcher.path(expected: String) = assertPath(expected)

    fun assertPath(expected: String) = assertEquals(expected, request.path)

    fun assertAnyQueryParam() =
        assertNotNull(request.queryParams, "Expected request query parameters to be present but none found.")

    infix fun MockRequestMatcher.queryParam(expectedKey: String): VerifierPair<Any> {
        assertQueryParam(expectedKey)
        return VerifierPair {
            assertQueryParam(expectedKey, it)
        }
    }

    fun assertQueryParam(expectedKey: String) = assertFalse(
        request.queryParams?.get(expectedKey)?.isEmpty() == true,
        """Expected request query parameter <"$expectedKey"> to be present but was not found."""
    )

    fun assertQueryParam(expectedKey: String, expectedValue: Any) {
        assertQueryParam(expectedKey)
        val actual = request.queryParams!!.get(expectedKey)
        if (actual.size == 1) {
            assertEquals(expectedValue, actual.first())
        } else {
            assertTrue(
                actual.contains(expectedValue),
                """Expected request query parameter <"$expectedKey"> to equals <"$expectedValue"> but value was not found."""
            )
        }
    }

    infix fun MockRequestMatcher.queryParams(expectedKey: String): VerifierPair<Collection<Any>> {
        assertQueryParam(expectedKey)
        return VerifierPair {
            assertQueryParams(expectedKey, it)
        }
    }

    fun assertQueryParams(expectedKey: String, expectedValues: Collection<Any>) {
        assertQueryParam(expectedKey)
        val actual = request.queryParams!!.get(expectedKey)
        assertTrue(
            expectedValues.containsAll(actual),
            """Expected request query parameter <"$expectedKey"> to equals <"$expectedValues"> but was <"$actual">"""
        )
    }

    infix fun MockRequestMatcher.queryParams(expected: Parameters) = assertQueryParams(expected)

    fun assertQueryParams(expectedQueryParams: Parameters) =
        assertEquals(expectedQueryParams, request.queryParams)

    fun assertAnyHeader() = assertNotNull(request.headers, "Expected request header to be present but none found.")

    infix fun MockRequestMatcher.header(expectedKey: String): VerifierPair<String> {
        assertHeader(expectedKey)
        return VerifierPair {
            assertHeader(expectedKey, it)
        }
    }

    fun assertHeader(expectedKey: String) {
        assertAnyHeader()
        assertFalse(
            request.headers!![expectedKey].isEmpty(),
            """Expected request header <"$expectedKey"> to be present but was not found."""
        )
    }

    fun assertHeader(expectedKey: String, expectedValue: String) {
        assertHeader(expectedKey)
        val actual = request.headers!![expectedKey]
        if (actual.size == 1) {
            assertEquals(expectedValue, actual.first())
        } else {
            assertTrue(
                actual.contains(expectedValue),
                """Expected request header <"$expectedKey"> to equals <"$expectedValue"> but values was not found."""
            )
        }
    }

    infix fun MockRequestMatcher.headers(expectedKey: String): VerifierPair<HeaderValues> {
        assertHeader(expectedKey)
        return VerifierPair {
            assertHeaders(expectedKey, it)
        }
    }

    fun assertHeaders(expectedKey: String, expectedValues: HeaderValues) {
        assertHeader(expectedKey)
        val actual = request.headers!![expectedKey]
        assertTrue(
            expectedValues.containsAll(actual),
            """Expected request header <"$expectedKey"> to equals <"$expectedValues"> but was <"$actual">"""
        )
    }

    infix fun MockRequestMatcher.headers(expected: Headers) = assertHeaders(expected)

    fun assertHeaders(expectedHeaders: Headers) =
        assertEquals(expectedHeaders.toString(), request.headers.toString())

    /**
     * Helper for neat infix syntax
     */
    class VerifierPair<T>(private val task: (T) -> Unit) {

        infix fun eq(second: T) = task(second)
    }
}