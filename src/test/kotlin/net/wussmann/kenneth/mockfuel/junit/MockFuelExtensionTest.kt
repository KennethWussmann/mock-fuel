package net.wussmann.kenneth.mockfuel.junit

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.toolbox.HttpClient
import io.mockk.every
import io.mockk.mockk
import net.wussmann.kenneth.mockfuel.MockFuelClient
import net.wussmann.kenneth.mockfuel.MockFuelStore
import net.wussmann.kenneth.mockfuel.data.MockResponse
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

internal class MockFuelExtensionTest {

    private val instance = MockFuelExtension()
    private lateinit var mockedParameterContext: ParameterContext

    @BeforeEach
    internal fun setUp() {
        mockedParameterContext = mockk(relaxed = true)
    }

    @Test
    fun `Should support MockFuelStore parameter`() {
        every { mockedParameterContext.parameter.type } returns MockFuelStore::class.java
        assertTrue(instance.supportsParameter(mockedParameterContext, mockk()))
    }

    @ParameterizedTest
    @ValueSource(classes = [String::class, File::class, MockFuelExtension::class])
    fun `Should not support any other class parameter`(clazz: Class<*>) {
        every { mockedParameterContext.parameter.type } returns clazz
        assertFalse(instance.supportsParameter(mockedParameterContext, mockk()))
    }

    @Test
    fun `Should resolve parameter to MockFuelStore`() {
        assertEquals(MockFuelStore::class, instance.resolveParameter(mockk(), mockk())::class)
    }

    @Test
    fun `Should override FuelManager global client beforeEach test`() {
        // enqueue something to identify this specific mockFuelStore
        val mockFuelStore = instance.resolveParameter(mockk(), mockk()) as MockFuelStore
        mockFuelStore.enqueue(MockResponse(statusCode = 200))

        instance.beforeEach(mockk())
        val response = FuelManager.instance.client.executeRequest(Fuel.get("http://fake.host/test"))

        assertEquals(MockFuelClient::class, FuelManager.instance.client::class)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `Should reset FuelManager global client and mockFuelStore afterEach test`() {
        // enqueue something to identify this specific mockFuelStore
        val mockFuelStore = instance.resolveParameter(mockk(), mockk()) as MockFuelStore
        mockFuelStore.enqueue(MockResponse(statusCode = 200))

        instance.afterEach(mockk())

        assertEquals(HttpClient::class, FuelManager.instance.client::class)
        assertEquals(0, mockFuelStore.responseQueue.size)
    }
}