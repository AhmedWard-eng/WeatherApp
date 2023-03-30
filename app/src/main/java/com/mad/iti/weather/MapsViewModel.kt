package com.mad.iti.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.FavWeatherRepoInterface
import com.mad.iti.weather.model.WeatherDataRepoInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsViewModel(
    private val weatherDataRepo: WeatherDataRepoInterface,
    private val _loc: WeatherLocationManager,
    private val favWeatherRepo: FavWeatherRepoInterface
) : ViewModel() {

    val location = _loc.location

    val isSaved = favWeatherRepo.favAddingWeatherFlow

    fun requestLocation(){
        _loc.requestLocation()
    }

    fun removeLocationUpdate(){
        _loc.removeLocationUpdate()
    }

    fun saveLocationToFav(latLng: LatLng){
        viewModelScope.launch(Dispatchers.IO) {
            favWeatherRepo.saveWeatherIntoFav(lat = latLng.latitude, long = latLng.longitude)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val weatherDataRepo: WeatherDataRepoInterface,
        private val _loc: WeatherLocationManager,
        private val favWeatherRepo: FavWeatherRepoInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
                MapsViewModel(weatherDataRepo, _loc, favWeatherRepo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}