package com.mad.iti.weather.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.model.FavAlertsWeatherRepoInterface
import com.mad.iti.weather.model.entities.AlertEntity
import kotlinx.coroutines.launch

class AlertViewModel(private val _repo: FavAlertsWeatherRepoInterface) : ViewModel() {

    val alerts = _repo.alertsWeatherFlow

    init {
        viewModelScope.launch {
            _repo.getAlerts()
        }

    }

    fun insertIntoAlerts(alertEntity: AlertEntity) {
        viewModelScope.launch {
            _repo.insertIntoAlerts(alertEntity)
        }
    }

    fun removeFromAlerts(alertEntity: AlertEntity) {
        viewModelScope.launch {
            _repo.removeFromAlerts(alertEntity)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val alertWeatherRepo: FavAlertsWeatherRepoInterface
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
                AlertViewModel(alertWeatherRepo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}