package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.WeatherEntity

sealed class APIStatus {
    class Success(var weatherEntity: WeatherEntity) : APIStatus()

    class Failure(var throwable: String) : APIStatus()

    object Loading : APIStatus()
}
