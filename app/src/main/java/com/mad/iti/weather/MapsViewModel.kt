package com.mad.iti.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.FavAlertsWeatherRepoInterface
import kotlinx.coroutines.launch

class MapsViewModel(
    private val _loc: WeatherLocationManager,
    private val favWeatherRepo: FavAlertsWeatherRepoInterface
) : ViewModel() {

    val location = _loc.location

    val isSaved = favWeatherRepo.favAddingWeatherFlow

//    val getItem = favWeatherRepo.

    fun requestLocationByGPS() {
        _loc.requestLocationByGPS()
    }

    fun updateAlert(entryId: String, lat: Double, long: Double) {
        viewModelScope.launch {
            favWeatherRepo.updateAlertItemLatLongById(entryId, lat, long)
        }
    }

    fun removeLocationUpdate() {
        _loc.removeLocationUpdate()
    }

    fun saveLocationToFav(latLng: LatLng) {
        viewModelScope.launch {
            favWeatherRepo.saveWeatherIntoFav(lat = latLng.latitude, long = latLng.longitude)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val _loc: WeatherLocationManager,
        private val favWeatherRepo: FavAlertsWeatherRepoInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
                MapsViewModel( _loc, favWeatherRepo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}