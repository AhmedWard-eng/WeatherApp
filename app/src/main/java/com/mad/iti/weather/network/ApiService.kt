package com.mad.iti.weather.network

import com.mad.iti.weather.model.OneCallWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/data/3.0/onecall?exclude=minutely&appid=8e3540a48d29fcaa9704ffd3b94bad07")
    suspend fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String
    ): Response<OneCallWeatherResponse>
}