package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.WeatherData

sealed class APIStatus {
    class Success(var weatherData: WeatherData) : APIStatus()

    class Failure(var throwable: String) : APIStatus()

    object Loading : APIStatus()
}
