# mock-fuel [![](https://jitpack.io/v/KennethWussmann/mock-fuel.svg)](https://jitpack.io/#KennethWussmann/mock-fuel) [![Build Status](https://travis-ci.org/KennethWussmann/mock-fuel.svg?branch=master)](https://travis-ci.org/KennethWussmann/mock-fuel) [![codecov](https://codecov.io/gh/KennethWussmann/mock-fuel/branch/master/graph/badge.svg)](https://codecov.io/gh/KennethWussmann/mock-fuel)

mock-fuel is a testing utility for the [Kotlin HTTP client Fuel](https://github.com/kittinunf/fuel). 

## Use case
When you want to unit- or integration-test with external dependencies you often have to mock the external HTTP requests.
In the same step you might also want to ensure the request to the external service is correct formatted and structured. 

This is where mock-fuel comes into place. Without having to spin-up a complete local web-server within your tests, you can
validate your requests and mock external calls.

### Benefits over alternatives
As the name suggests `mock-fuel` is made for that one HTTP client `Fuel`. When you use something else, this is not the best solution for you.
But if you use Fuel and look for alternatives there are:
* Mocking each response with a common mock framework
    * Can be frustrating because of Fuel's API
* Using something like [mock-server](http://mock-server.com/) or [okhttp mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
    * Always spins-up a local web-server which takes time and increases your test runtime a lot
    
Because mock-fuel directly overrides how Fuel is sending HTTP requests it is significant faster than starting a local webserver for every test.

### Downsides
The biggest benefit is also a pitfall. Be aware that mock-fuel is overriding the default `Client` of Fuel.
That might lead to different exception handling and when you also use your own `Client` implementation for Fuel mock-fuel will not work for you.

### Requirements
* Fuel `>2.0.0`
* JUnit `>5.0`
* Kotlin `>1.3.0`

## Usage [![](https://jitpack.io/v/KennethWussmann/mock-fuel.svg)](https://jitpack.io/#KennethWussmann/mock-fuel)

Setup your test dependencies to include mock-fuel. Make sure that you also provide an own version of Fuel. 

<details><summary>Maven</summary>
<p>

Replace `Version` with above latest version.

```XML
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```XML
<dependencies>
    <dependency>
        <groupId>com.github.KennethWussmann</groupId>
        <artifactId>mock-fuel</artifactId>
        <version>VERSION</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

</p>
</details>

<details><summary>Gradle</summary>
<p>

Replace `Version` with above latest version.


```Groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    testCompile 'com.github.KennethWussmann:mock-fuel:VERSION' 
}
```

</p>
</details>

### Getting started
To setup mock-fuel simply annotate your test class with `@ExtendWith`:

```kotlin
@ExtendWith(MockFuelExtension::class)
internal class MyAwesomeTest 
```

Now, every time Fuel is emitting requests mock-fuel will catch them and send mock responses instead.

To change what mock-fuel returns you add `MockFuelStore` as a parameter to your test in the class that is annotated with the above annotation:
```kotlin
@ExtendWith(MockFuelExtension::class)
internal class MyAwesomeTest {
    @Test
    fun `Should use mock-fuel`(mockFuelStore: MockFuelStore) {
       // mockFuelStore contains every response that is served to incoming requests.
       // How to use it is described below.
    }
}
```

### Enqueue
Enqueuing is the simplest. You just put a response to a queue and every time Fuel is doing a request the first element of the queue
will be served as response. Enqueued responses will be removed from the queue when they were served to a request.

```kotlin
@Test
fun `Should return expected body without doing real http requests`(mockFuelStore: MockFuelStore) {
    val expectedBody = """
        {
            "hello": "world"
        }
    """.trimIndent()

    // we enqueue this response to be served when any request comes in
    mockFuelStore.enqueue(
            MockResponse(
                    statusCode = 200,
                    body = expectedBody.toByteArray()
            )
    )

    // make any request
    val (_, response, result) = Fuel.get("/test").responseString()

    assertEquals(200, response.statusCode)
    assertEquals(expectedBody, result.get())
}
```

### Dispatching
Dispatching allows you to serve certain responses when expected requests where made.

```kotlin
@Test
fun `Should return response for certain request`(mockFuelStore: MockFuelStore) {
    mockFuelStore.on(method = Method.POST, path = "/test") {
            MockResponse(
                    statusCode = 200,
                    body = """{ "success": true }""".toByteArray()
            )
    }

    val (_, response, result) = Fuel
            .post("/test")
            .body("{}")
            .responseString()

    assertEquals(200, response.statusCode)
    assertEquals("""{ "success": true }""", result.get())
}
```

### Verify requests
mock-fuel comes with some utility assertions to ensure that the request that was sent meets certain requirements.

```kotlin
@Test
fun `Should ensure request structure`(mockFuelStore: MockFuelStore) {
    mockFuelStore.enqueue(MockResponse(statusCode = 200))
    
    val (_, response, _) = Fuel
            .post("/test", parameters = listOf("abc" to "123", "abc" to "456"))
            .body("{}")
            .header("X-Example" to "hello_world")
            .responseString()
    
    // verify that there was a request.
    // requests are also in a queue and calling verifyRequest takes & removes the first
    mockFuelStore.verifyRequest {
        // it should be a POST
        assertMethod(Method.POST)
        // to /test
        assertPath("/test")
        // with the queryParameter abc=123 & abc=456
        assertQueryParams("abc", listOf("123", "456"))
        // and the header X-Example: hello_world
        assertHeader("X-Example", "hello_world")
        // more assertions available ...
    }
    
    assertEquals(200, response.statusCode)
}
```