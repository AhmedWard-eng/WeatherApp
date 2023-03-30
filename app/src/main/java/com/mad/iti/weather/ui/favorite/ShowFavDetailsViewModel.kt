package com.mad.iti.weather.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.model.FavWeatherRepoInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowFavDetailsViewModel(private val _favWeatherRepo: FavWeatherRepoInterface) : ViewModel() {


    val weatherFlow = _favWeatherRepo.favWeatherFlow

    fun getWeather(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _favWeatherRepo.updateFlowWithCurrentData(id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val favWeatherRepo: FavWeatherRepoInterface
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