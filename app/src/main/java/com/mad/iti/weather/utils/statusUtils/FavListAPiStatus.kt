package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.FavWeatherData

sealed class FavListAPiStatus {
    class Success(var favWeatherDataList: List<FavWeatherData>) : FavListAPiStatus()

    class Failure(var throwable: String) : FavListAPiStatus()

    object Loading : FavListAPiStatus()
}