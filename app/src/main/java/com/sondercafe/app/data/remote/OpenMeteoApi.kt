package com.sondercafe.app.data.remote

import com.sondercafe.app.data.remote.dto.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo forecast API (https://open-meteo.com/en/docs). Free for non-commercial use and
 * needs no API key, so there's no secret to protect in the app.
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String
    ): OpenMeteoResponse
}
