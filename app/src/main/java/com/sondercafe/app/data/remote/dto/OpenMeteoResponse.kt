package com.sondercafe.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The parts of the Open-Meteo forecast response the app uses. Everything else in the response
 * is ignored, so new fields added by the API won't break parsing.
 *
 * Example: {"current": {"time": "2026-11-12T14:00", "temperature_2m": 9.4, "weather_code": 61}}
 */
@Serializable
data class OpenMeteoResponse(
    val current: CurrentWeatherDto? = null
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null
)
