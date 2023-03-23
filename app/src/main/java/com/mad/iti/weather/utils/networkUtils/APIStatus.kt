package com.mad.iti.weather.utils.networkUtils

import com.mad.iti.weather.model.weather.OneCallWeatherResponse

sealed class APIStatus {
    class Success(var oneCallWeatherResponse: OneCallWeatherResponse) : APIStatus()

    class Failure(var throwable: String) : APIStatus()

    object Loading : APIStatus()
}
