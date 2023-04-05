package com.mad.iti.weather.utils.statusUtils

import com.mad.iti.weather.model.entities.FavWeatherEntity


sealed class FavAPIStatus {
    class Success(var favWeatherEntity: FavWeatherEntity) : FavAPIStatus()

    class Failure(var throwable: String) : FavAPIStatus()

    object Loading : FavAPIStatus()
}