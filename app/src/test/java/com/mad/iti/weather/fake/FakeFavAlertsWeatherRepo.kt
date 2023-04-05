package com.mad.iti.weather.fake

import com.mad.iti.weather.model.FavAlertsWeatherRepoInterface
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import com.mad.iti.weather.utils.statusUtils.AlertsAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

class FakeFavAlertsWeatherRepo : FavAlertsWeatherRepoInterface {
    private val alertsList = mutableListOf<AlertEntity>()
    override val favWeatherListFlow: StateFlow<FavListAPiStatus>
        get() = TODO("Not yet implemented")

    override suspend fun saveWeatherIntoFav(lat: Double, long: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun updateWeatherFavInfo(favWeatherEntity: FavWeatherEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun getWeatherFavData() {
        TODO("Not yet implemented")
    }

    override val favAddingWeatherFlow: StateFlow<AddingFavAPIStatus>
        get() = TODO("Not yet implemented")

    override suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity) {
        TODO("Not yet implemented")
    }

    override val favWeatherFlow: StateFlow<FavAPIStatus>
        get() = TODO("Not yet implemented")

    override suspend fun updateFlowWithCurrentData(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun insertIntoAlerts(alertEntity: AlertEntity) {
        alertsList.add(alertEntity)
    }

    override suspend fun removeFromAlerts(alertEntity: AlertEntity) {
        alertsList.remove(alertsList.first { it.id == alertEntity.id })
    }

    private val _alertsWeahterFlow = MutableStateFlow<AlertsAPIStatus>(AlertsAPIStatus.Loading)
    override val alertsWeatherFlow: StateFlow<AlertsAPIStatus>
        get() = _alertsWeahterFlow

    override suspend fun getAlerts() {
        _alertsWeahterFlow.emit(AlertsAPIStatus.Success(alertsList))
    }


    override suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double) {
        val alert = alertsList.first { it.id == entryId }
        val updatedAlert = alert.copy(lat = lat, lon = long)
        alertsList.remove(alert)
        alertsList.add(updatedAlert)
    }

    override fun getAlertWithId(entryId: String): AlertEntity {
        TODO("Not yet implemented")
    }

    override suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse> {
        TODO("Not yet implemented")
    }
}