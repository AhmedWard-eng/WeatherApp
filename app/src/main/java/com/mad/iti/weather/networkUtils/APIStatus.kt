package com.mad.iti.weather.networkUtils

import com.mad.iti.weather.model.OneCallWeatherResponse
import com.mad.iti.weather.network.ApiService

sealed class APIStatus {
    class Success(var oneCallWeatherResponse: OneCallWeatherResponse) : APIStatus()

    class Failure(var throwable: String) : APIStatus()

    object Loading : APIStatus()
}
