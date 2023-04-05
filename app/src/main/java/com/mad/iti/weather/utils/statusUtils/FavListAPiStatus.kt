package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.FavWeatherEntity

sealed class FavListAPiStatus {
    class Success(var favWeatherEntityList: List<FavWeatherEntity>) : FavListAPiStatus()
    class Failure(var throwable: String) : FavListAPiStatus()
    object Loading : FavListAPiStatus()
}