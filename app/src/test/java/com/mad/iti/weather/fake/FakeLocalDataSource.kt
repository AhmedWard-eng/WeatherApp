package com.mad.iti.weather.fake

import com.mad.iti.weather.db.LocalDataSource
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.entities.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalDataSource(
    private val alerts: MutableList<AlertEntity>,
    private val favs: MutableList<FavWeatherEntity>
) : LocalDataSource {

    override fun getWeatherData(): Flow<WeatherEntity> {
        TODO("Not yet implemented")
    }

    override fun insertWeatherData(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

    override fun getWeatherFav(): Flow<List<FavWeatherEntity>?> {
        return flowOf(favs.toList())
    }

    override fun insertIntoFav(favWeatherEntity: FavWeatherEntity) {
        favs.add(favWeatherEntity)
    }

    override suspend fun updateFavItemWithCurrentWeather(weather: FavWeatherEntity) {
        val fav = favs.first { it.id == weather.id }
        favs.remove(fav)
        favs.add(weather)
    }

    override suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity) {
        val fav = favs.first { it.id == favWeatherEntity.id }
        favs.remove(fav)
    }

    override fun getFavWeatherWithId(entryId: String): Flow<FavWeatherEntity> {
        val fav = favs.first { it.id == entryId }
        return flowOf(fav)
    }

    override suspend fun insertIntoAlert(alertEntity: AlertEntity) {
        alerts.add(alertEntity)
    }

    override suspend fun removeFromAlerts(alertEntity: AlertEntity) {
        alerts.remove(alerts.first { it.id == alertEntity.id })
    }

    override fun getAlerts(): Flow<List<AlertEntity>> {
        return flowOf(alerts.toList())
    }

    override suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double) {
        val alert = alerts.first { it.id == entryId }
        val updatedAlert = alert.copy(lat = lat, lon = long)
        alerts.remove(alert)
        alerts.add(updatedAlert)
    }

    override fun getAlertWithId(entryId: String): AlertEntity {
        return alerts.first { it.id == entryId }
    }
}