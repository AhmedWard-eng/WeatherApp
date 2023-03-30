package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.FavWeatherData


sealed class FavAPIStatus {
    class Success(var favWeatherData: FavWeatherData) : FavAPIStatus()

    class Failure(var throwable: String) : FavAPIStatus()

    object Loading : FavAPIStatus()
}