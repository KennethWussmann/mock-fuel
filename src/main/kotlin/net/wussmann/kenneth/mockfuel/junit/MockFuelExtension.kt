package net.wussmann.kenneth.mockfuel.junit

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.toolbox.HttpClient
import net.wussmann.kenneth.mockfuel.MockFuelClient
import net.wussmann.kenneth.mockfuel.MockFuelStore
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

/**
 * JUnit 5 Extension to mock Fuel requests with responses
 */
class MockFuelExtension : ParameterResolver, BeforeEachCallback, AfterEachCallback {

    private val mockFuelStore = MockFuelStore()

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == MockFuelStore::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return mockFuelStore
    }

    override fun beforeEach(context: ExtensionContext) {
        FuelManager.instance.client = MockFuelClient(mockFuelStore)
    }

    override fun afterEach(context: ExtensionContext) {
        FuelManager.instance.client = HttpClient(FuelManager.instance.proxy, hook = FuelManager.instance.hook)
        mockFuelStore.reset()
    }
}
