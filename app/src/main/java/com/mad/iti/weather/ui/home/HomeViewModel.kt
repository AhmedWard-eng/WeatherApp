package com.mad.iti.weather.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.OneCallRepoInterface
import com.mad.iti.weather.utils.networkUtils.APIStatus
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "HomeFragment"

class HomeViewModel(private val _repo: OneCallRepoInterface,_locManager : WeatherLocationManager) : ViewModel() {

    val weather: StateFlow<APIStatus>
        get() = _repo.weatherFlow
    val loc = _locManager.location



    @Suppress("UNCHECKED_CAST")
    class Factory(private val _repo: OneCallRepoInterface, private val _locManager: WeatherLocationManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(_repo,_locManager) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}