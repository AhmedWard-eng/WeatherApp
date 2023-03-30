package com.mad.iti.weather.ui.home


import androidx.lifecycle.*
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.WeatherDataRepoInterface
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"

class HomeViewModel(
    private val _repo: WeatherDataRepoInterface,
    private val _weatherLocationManager: WeatherLocationManager
) : ViewModel() {

    val weather: StateFlow<APIStatus>
        get() = _repo.weatherFlow
    val location = _weatherLocationManager.location

    fun getWeather(lat: Double, long: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.enqueueWeatherCall("$lat", "$long")
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val _repo: WeatherDataRepoInterface,
        private val _weatherLocationManager: WeatherLocationManager
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