package com.sondercafe.app.domain.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.WeatherSnapshot

interface WeatherRepository {

    /** Current weather at the café. May return a recent cached value instead of calling the API. */
    suspend fun getCurrentWeather(): AppResult<WeatherSnapshot>
}
