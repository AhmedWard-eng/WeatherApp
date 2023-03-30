package com.mad.iti.weather.ui.favorite

import androidx.lifecycle.*
import com.mad.iti.weather.model.FavWeatherRepoInterface
import com.mad.iti.weather.model.entities.FavWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavViewModel(
    private val favWeatherRepo: FavWeatherRepoInterface
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            favWeatherRepo.getWeatherFavData()
        }
    }

    val favWeatherData = favWeatherRepo.favWeatherListFlow

    fun deleteItemFromFav(favWeatherData: FavWeatherData) {
        viewModelScope.launch(Dispatchers.IO) {
            favWeatherRepo.removeWeatherFromFav(favWeatherData)
        }
    }

    fun updateWeatherFavInfo(favWeatherData: FavWeatherData) {
        viewModelScope.launch(Dispatchers.IO) {
            favWeatherRepo.updateWeatherFavInfo(favWeatherData)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val favWeatherRepo: FavWeatherRepoInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(FavViewModel::class.java)) {
                FavViewModel(favWeatherRepo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}