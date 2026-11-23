package com.sondercafe.app.data.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.data.remote.OpenMeteoApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

class WeatherRepositoryImplTest {

    private class MutableClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
        fun advance(duration: Duration) {
            now = now.plus(duration)
        }
    }

    private lateinit var server: MockWebServer
    private lateinit var clock: MutableClock
    private lateinit var repository: WeatherRepositoryImpl

    private val rainyResponse = """
        {
          "latitude": 52.64,
          "longitude": -1.14,
          "current_units": {"temperature_2m": "°C"},
          "current": {"time": "2026-11-12T14:00", "interval": 900, "temperature_2m": 9.4, "weather_code": 61}
        }
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        clock = MutableClock(Instant.parse("2026-11-12T14:00:00Z"))

        val client = OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenMeteoApi::class.java)

        repository = WeatherRepositoryImpl(api, clock)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `parses current weather and ignores unknown fields`() = runTest {
        enqueueJson(rainyResponse)

        val result = repository.getCurrentWeather()

        val weather = (result as AppResult.Success).data
        assertEquals(9.4, weather.temperatureCelsius, 0.0)
        assertEquals(61, weather.weatherCode)
        assertTrue(weather.isWet)
    }

    @Test
    fun `asks for the cafe location and the fields it needs`() = runTest {
        enqueueJson(rainyResponse)

        repository.getCurrentWeather()

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/forecast", url.encodedPath)
        assertEquals(WeatherRepositoryImpl.CAFE_LATITUDE.toString(), url.queryParameter("latitude"))
        assertEquals(WeatherRepositoryImpl.CAFE_LONGITUDE.toString(), url.queryParameter("longitude"))
        assertEquals("temperature_2m,weather_code", url.queryParameter("current"))
    }

    @Test
    fun `reuses a fresh result without calling the API again`() = runTest {
        enqueueJson(rainyResponse)

        repository.getCurrentWeather()
        clock.advance(Duration.ofMinutes(10))
        repository.getCurrentWeather()

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `fetches again once the cache is stale`() = runTest {
        enqueueJson(rainyResponse)
        enqueueJson(rainyResponse.replace("9.4", "11.0"))

        repository.getCurrentWeather()
        clock.advance(Duration.ofMinutes(31))
        val second = repository.getCurrentWeather()

        assertEquals(2, server.requestCount)
        assertEquals(11.0, (second as AppResult.Success).data.temperatureCelsius, 0.0)
    }

    @Test
    fun `falls back to a recent result when the API fails`() = runTest {
        enqueueJson(rainyResponse)
        enqueueJson("", code = 503)

        repository.getCurrentWeather()
        clock.advance(Duration.ofMinutes(45))
        val result = repository.getCurrentWeather()

        assertEquals(9.4, (result as AppResult.Success).data.temperatureCelsius, 0.0)
    }

    @Test
    fun `reports a server error when there is nothing cached`() = runTest {
        enqueueJson("", code = 500)

        val result = repository.getCurrentWeather()

        assertTrue((result as AppResult.Error).message.contains("unavailable"))
    }

    @Test
    fun `reports a connection problem`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.getCurrentWeather()

        assertEquals("No connection to the weather service", (result as AppResult.Error).message)
    }

    @Test
    fun `reports malformed json`() = runTest {
        enqueueJson("{ not json")

        val result = repository.getCurrentWeather()

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `rejects a response without current conditions`() = runTest {
        enqueueJson("""{"latitude": 52.6}""")

        val result = repository.getCurrentWeather()

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `rejects implausible values`() = runTest {
        enqueueJson(rainyResponse.replace("9.4", "999.0"))

        val result = repository.getCurrentWeather()

        assertTrue(result is AppResult.Error)
    }
}
