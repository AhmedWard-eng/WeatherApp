package com.mad.iti.weather.ui.favorite

import androidx.lifecycle.*
import com.mad.iti.weather.model.FavAlertsWeatherRepoInterface
import com.mad.iti.weather.model.entities.FavWeatherEntity
import kotlinx.coroutines.launch

class FavViewModel(
    private val favWeatherRepo: FavAlertsWeatherRepoInterface
) : ViewModel() {

    init {
        viewModelScope.launch {
            favWeatherRepo.getWeatherFavData()
        }
    }

    val favWeatherData = favWeatherRepo.favWeatherListFlow

    fun deleteItemFromFav(favWeatherEntity: FavWeatherEntity) {
        viewModelScope.launch{
            favWeatherRepo.removeWeatherFromFav(favWeatherEntity)
        }
    }

    fun updateWeatherFavInfo(favWeatherEntity: FavWeatherEntity) {
        viewModelScope.launch{
            favWeatherRepo.updateWeatherFavInfo(favWeatherEntity)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val favWeatherRepo: FavAlertsWeatherRepoInterface
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