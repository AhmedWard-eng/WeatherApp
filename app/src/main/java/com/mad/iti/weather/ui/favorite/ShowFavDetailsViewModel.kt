package com.mad.iti.weather.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.model.FavAlertsWeatherRepoInterface
import kotlinx.coroutines.launch

class ShowFavDetailsViewModel(private val _favWeatherRepo: FavAlertsWeatherRepoInterface) : ViewModel() {


    val weatherFlow = _favWeatherRepo.favWeatherFlow

    fun getWeather(id: String) {
        viewModelScope.launch {
            _favWeatherRepo.updateFlowWithCurrentData(id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val favWeatherRepo: FavAlertsWeatherRepoInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ShowFavDetailsViewModel::class.java)) {
                ShowFavDetailsViewModel(favWeatherRepo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}