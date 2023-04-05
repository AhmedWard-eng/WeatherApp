package com.mad.iti.weather.fake

import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.network.APIClientInterface
import retrofit2.Response

class FakeSuccessApiClient(val oneCallWeatherResponse: OneCallWeatherResponse) :
    APIClientInterface {

    override suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse> {
        val response = oneCallWeatherResponse.copy(lat = lat.toDouble(), lon = lon.toDouble())
        return Response.success(response)
    }
}
