package com.mad.iti.weather.db

import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.entities.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWeatherData(): Flow<WeatherEntity?>

    fun insertWeatherData(weatherEntity: WeatherEntity)

    fun getWeatherFav(): Flow<List<FavWeatherEntity>?>

    fun insertIntoFav(favWeatherEntity: FavWeatherEntity)

    suspend fun updateFavItemWithCurrentWeather(weather: FavWeatherEntity)
    suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity)
    fun getFavWeatherWithId(entryId: String): Flow<FavWeatherEntity>
    suspend fun insertIntoAlert(alertEntity: AlertEntity)
    suspend fun removeFromAlerts(alertEntity: AlertEntity)
    fun getAlerts(): Flow<List<AlertEntity>>

    suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double)

    fun getAlertWithId(entryId: String): AlertEntity
}