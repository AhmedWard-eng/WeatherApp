package com.mad.iti.weather.model

import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.utils.statusUtils.*
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

interface FavAlertsWeatherRepoInterface {
    val favWeatherListFlow: StateFlow<FavListAPiStatus>

    suspend fun saveWeatherIntoFav(lat: Double, long: Double)

    suspend fun updateWeatherFavInfo(favWeatherEntity: FavWeatherEntity)

    suspend fun getWeatherFavData()
    val favAddingWeatherFlow: StateFlow<AddingFavAPIStatus>
    suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity)
    val favWeatherFlow: StateFlow<FavAPIStatus>
    suspend fun updateFlowWithCurrentData(id: String)
    suspend fun insertIntoAlerts(alertEntity: AlertEntity)
    suspend fun removeFromAlerts(alertEntity: AlertEntity)
    suspend fun getAlerts()
    val alertsWeatherFlow: StateFlow<AlertsAPIStatus>
    suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double)

    fun getAlertWithId(entryId: String): AlertEntity
    suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse>
}