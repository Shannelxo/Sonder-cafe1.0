package com.sondercafe.app.domain.model

import java.time.Instant

/**
 * Current weather at the café. [weatherCode] is a WMO weather interpretation code, as returned
 * by Open-Meteo (0 = clear sky, 61 = rain, and so on).
 */
data class WeatherSnapshot(
    val temperatureCelsius: Double,
    val weatherCode: Int,
    val fetchedAt: Instant
) {
    val isWet: Boolean
        get() = weatherCode in 51..67 || weatherCode in 80..82 || weatherCode in 95..99

    val conditions: String
        get() = when (weatherCode) {
            0 -> "clear"
            in 1..3 -> "cloudy"
            45, 48 -> "foggy"
            in 51..67, in 80..82 -> "rainy"
            in 71..77, 85, 86 -> "snowy"
            in 95..99 -> "stormy"
            else -> "changeable"
        }
}
