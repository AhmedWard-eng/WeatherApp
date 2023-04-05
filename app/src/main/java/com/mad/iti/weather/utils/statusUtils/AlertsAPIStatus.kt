package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.weather.OneCallWeatherResponse


sealed class AlertsAPIStatus {
    class Success(var alertEntityList: List<AlertEntity>) : AlertsAPIStatus()

    class Failure(var throwable: String) : AlertsAPIStatus()

    object Loading : AlertsAPIStatus()
}

