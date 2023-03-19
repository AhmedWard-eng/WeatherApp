package com.mad.iti.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.model.OneCallRepo
import com.mad.iti.weather.model.OneCallRepoInterface
import com.mad.iti.weather.networkUtils.APIStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val _repo: OneCallRepoInterface) : ViewModel() {


    fun getWeather(lat: String, lon: String) {
        viewModelScope.launch {
            _repo.enqueueWeatherCall(lat, lon)
        }
    }

    class Factory(private val _repo: OneCallRepoInterface) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(_repo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}