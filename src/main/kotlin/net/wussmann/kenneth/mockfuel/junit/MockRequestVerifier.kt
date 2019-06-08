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
class MockRequestVerifier(private val mockRequestMatcher: MockRequestMatcher) {

    private fun findQueryParameterValues(key: String): List<Any?> {
        assertAnyQueryParam()
        requireNotNull(mockRequestMatcher.queryParams)
        return mockRequestMatcher.queryParams.filter { it.first == key }.map { it.second }
    }

    fun assertAnyBody() = assertNotNull(mockRequestMatcher.body, "Expected request body to be present but none found.")

    fun assertBody(expected: ByteArray) {
        assertAnyBody()
        requireNotNull(mockRequestMatcher.body)
        assertTrue(expected.contentEquals(mockRequestMatcher.body), "Expected body byte array contents to match.")
    }

    fun assertBody(expected: String) {
        assertAnyBody()
        requireNotNull(mockRequestMatcher.body)
        assertEquals(expected, String(mockRequestMatcher.body))
    }

    fun assertMethod(expected: Method) = assertEquals(expected, mockRequestMatcher.method)

    fun assertHost(expected: String) = assertEquals(expected, mockRequestMatcher.host)

    fun assertPath(expected: String) = assertEquals(expected, mockRequestMatcher.path)

    fun assertAnyQueryParam() =
        assertNotNull(mockRequestMatcher.queryParams, "Expected request query parameters to be present but none found.")

    fun assertQueryParam(expectedKey: String) = assertFalse(
        findQueryParameterValues(expectedKey).isEmpty(),
        """Expected request query parameter <"$expectedKey"> to be present but was not found."""
    )

    fun assertQueryParam(expectedKey: String, expectedValue: Any) {
        assertQueryParam(expectedKey)
        val actual = findQueryParameterValues(expectedKey)
        if (actual.size == 1) {
            assertEquals(expectedValue, actual.first())
        } else {
            assertTrue(
                actual.contains(expectedValue),
                """Expected request query parameter <"$expectedKey"> to equals <"$expectedValue"> but value was not found."""
            )
        }
    }

    fun assertQueryParams(expectedKey: String, expectedValues: Collection<Any>) {
        assertQueryParam(expectedKey)
        val actual = findQueryParameterValues(expectedKey)
        assertTrue(
            expectedValues.containsAll(actual),
            """Expected request query parameter <"$expectedKey"> to equals <"$expectedValues"> but was <"$actual">"""
        )
    }

    fun assertQueryParams(expectedQueryParams: Parameters) =
        assertEquals(expectedQueryParams, mockRequestMatcher.queryParams)

    fun assertAnyHeader() =
        assertNotNull(mockRequestMatcher.headers, "Expected request header to be present but none found.")

    fun assertHeader(expectedKey: String) = {
        assertAnyHeader()
        requireNotNull(mockRequestMatcher.headers)
        assertFalse(
            mockRequestMatcher.headers[expectedKey].isEmpty(),
            """Expected request header <"$expectedKey"> to be present but was not found."""
        )
    }

    fun assertHeader(expectedKey: String, expectedValue: String) {
        assertHeader(expectedKey)
        requireNotNull(mockRequestMatcher.headers)
        val actual = mockRequestMatcher.headers[expectedKey]
        if (actual.size == 1) {
            assertEquals(expectedValue, actual.first())
        } else {
            assertTrue(
                actual.contains(expectedValue),
                """Expected request header <"$expectedKey"> to equals <"$expectedValue"> but values was not found."""
            )
        }
    }

    fun assertHeaders(expectedKey: String, expectedValues: HeaderValues) {
        assertHeader(expectedKey)
        requireNotNull(mockRequestMatcher.headers)
        val actual = mockRequestMatcher.headers[expectedKey]
        assertTrue(
            expectedValues.containsAll(actual),
            """Expected request header <"$expectedKey"> to equals <"$expectedValues"> but was <"$actual">"""
        )
    }

    fun assertHeaders(expectedHeaders: Headers) =
        assertEquals(expectedHeaders.toString(), mockRequestMatcher.headers.toString())
}
}