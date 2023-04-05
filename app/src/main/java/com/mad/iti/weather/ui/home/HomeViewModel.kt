package com.mad.iti.weather.ui.home


import androidx.lifecycle.*
import com.mad.iti.weather.location.WeatherLocationManagerInterface
import com.mad.iti.weather.model.WeatherDataRepoInterface
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val _repo: WeatherDataRepoInterface,
    private val _weatherLocationManager: WeatherLocationManagerInterface
) : ViewModel() {

    val weather: StateFlow<APIStatus>
        get() = _repo.weatherFlow
    val location = _weatherLocationManager.location


    init {
        viewModelScope.launch {
            _repo.getWeatherData()
        }
    }

    fun getWeather(lat: Double, long: Double) {
        viewModelScope.launch {
            _repo.refreshWeatherCall("$lat", "$long")
        }
    }

    fun isLocationEnabled(): Boolean{
        return _weatherLocationManager.isLocationEnabled()
    }
    fun requestLocation(){
        _weatherLocationManager.requestLocationByGPS()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val _repo: WeatherDataRepoInterface,
        private val _weatherLocationManager: WeatherLocationManagerInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(_repo, _weatherLocationManager) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}