package com.mad.iti.weather.fake

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.model.LatLng
import com.mad.iti.weather.location.WeatherLocationManagerInterface
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWeatherLocManager(
    private val latGps: Double,
    private val lonGps: Double,
    private val latMap: Double,
    private val lonMap: Double
) : WeatherLocationManagerInterface {
    private val _location = MutableStateFlow<LocationStatus>(LocationStatus.Loading)
    override val location: StateFlow<LocationStatus>
        get() = _location


    override fun requestLocationByGPS() {
        _location.tryEmit(LocationStatus.Success(LatLng(latGps, lonGps)))
    }

    override fun requestLocationByGPS(callback: (LatLng) -> Unit) {
    }



    override fun isLocationEnabled(): Boolean {
        return true
    }

    override fun requestLocationSavedFromMap() {
        _location.tryEmit(LocationStatus.Success(LatLng(latMap, lonMap)))
    }

    override fun removeLocationUpdate(locationCallback: LocationCallback) {
    }

}