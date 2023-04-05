package com.mad.iti.weather.location

import android.annotation.SuppressLint
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import kotlinx.coroutines.flow.StateFlow

interface WeatherLocationManagerInterface {
    val location: StateFlow<LocationStatus>

    @SuppressLint("MissingPermission")
    fun requestLocationByGPS()
    fun removeLocationUpdate()
    fun isLocationEnabled(): Boolean
    fun requestLocationSavedFromMap()
}