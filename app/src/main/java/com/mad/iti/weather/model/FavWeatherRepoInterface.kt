package com.mad.iti.weather.model

import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.flow.StateFlow

interface FavWeatherRepoInterface {
    val favWeatherListFlow: StateFlow<FavListAPiStatus>

    suspend fun saveWeatherIntoFav(lat: Double, long: Double)

    suspend fun updateWeatherFavInfo(favWeatherData: FavWeatherData)

    suspend fun getWeatherFavData()
    val favAddingWeatherFlow: StateFlow<AddingFavAPIStatus>
    suspend fun removeWeatherFromFav(favWeatherData: FavWeatherData)
    val favWeatherFlow: StateFlow<FavAPIStatus>
    suspend fun updateFlowWithCurrentData(id: String)
}