package com.mad.iti.weather.network

import android.util.Log
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val retrofit: Retrofit by lazy {
    Retrofit.Builder().baseUrl("https://api.openweathermap.org")
        .addConverterFactory(GsonConverterFactory.create()).build()
}
val apiService: ApiService by lazy {
    retrofit.create(ApiService::class.java)
}

object APIClient : APIClientInterface{
    override suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse> {
        Log.d("TAG", "getWeather: ${getLanguageLocale()}")
        return apiService.getWeather(lat = lat, lon = lon, getLanguageLocale())
    }
}

interface APIClientInterface {
    suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse>
}
