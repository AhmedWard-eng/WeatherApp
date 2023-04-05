package com.mad.iti.weather.model

import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.flow.StateFlow

interface WeatherDataRepoInterface {
   suspend fun refreshWeatherCall(lat: String, lon: String)
    val weatherFlow: StateFlow<APIStatus>


    suspend fun getWeatherData()
}
