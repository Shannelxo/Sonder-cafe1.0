package com.sondercafe.app.data.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.data.remote.OpenMeteoApi
import com.sondercafe.app.domain.model.WeatherSnapshot
import com.sondercafe.app.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches the current weather at the café and caches it.
 *
 * - A result less than [FRESH_FOR] old is reused without a network call. Weather doesn't change
 *   minute to minute, and this saves data and battery.
 * - If a call fails, a result up to [USABLE_FOR] old is still returned rather than nothing.
 * - Otherwise the error explains what went wrong and recommendations carry on without weather.
 *
 * The café's own coordinates are used, so the app never needs the user's location.
 */
@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
    private val clock: Clock
) : WeatherRepository {

    private val mutex = Mutex()
    private var cached: WeatherSnapshot? = null

    override suspend fun getCurrentWeather(): AppResult<WeatherSnapshot> = mutex.withLock {
        cached?.takeIf { ageOf(it) < FRESH_FOR }?.let { return@withLock AppResult.Success(it) }

        val result = fetch()
        if (result is AppResult.Error) {
            cached?.takeIf { ageOf(it) < USABLE_FOR }?.let { return@withLock AppResult.Success(it) }
        }
        result
    }

    private suspend fun fetch(): AppResult<WeatherSnapshot> = try {
        val current = api.getForecast(CAFE_LATITUDE, CAFE_LONGITUDE, CURRENT_FIELDS).current
        val temperature = current?.temperature
        val code = current?.weatherCode

        when {
            temperature == null || code == null ->
                AppResult.Error("The weather service didn't return current conditions")
            temperature !in PLAUSIBLE_CELSIUS || code !in 0..99 ->
                AppResult.Error("The weather service returned values that don't look right")
            else -> {
                val snapshot = WeatherSnapshot(temperature, code, clock.instant())
                cached = snapshot
                AppResult.Success(snapshot)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Error("No connection to the weather service", e)
    } catch (e: HttpException) {
        AppResult.Error("The weather service is unavailable right now (error ${e.code()})", e)
    } catch (e: SerializationException) {
        AppResult.Error("The weather service sent data the app couldn't read", e)
    }

    private fun ageOf(snapshot: WeatherSnapshot): Duration = Duration.between(snapshot.fetchedAt, clock.instant())

    companion object {
        // Leicester city centre. Change these to the café's real location.
        const val CAFE_LATITUDE = 52.6369
        const val CAFE_LONGITUDE = -1.1398
        const val CURRENT_FIELDS = "temperature_2m,weather_code"

        val FRESH_FOR: Duration = Duration.ofMinutes(30)
        val USABLE_FOR: Duration = Duration.ofHours(3)
        private val PLAUSIBLE_CELSIUS = -60.0..60.0
    }
}
