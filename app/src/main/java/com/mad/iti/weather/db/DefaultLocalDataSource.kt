package com.mad.iti.weather.db

import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.entities.WeatherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DefaultLocalDataSource private constructor(private val _weatherDao: WeatherDao) :
    LocalDataSource {
    override fun getWeatherData(): Flow<WeatherEntity> {
        return _weatherDao.getWeather()
    }

    override fun insertWeatherData(weatherEntity: WeatherEntity) {
        _weatherDao.insert(weatherEntity)
    }

    override fun getWeatherFav(): Flow<List<FavWeatherEntity>> {
        return _weatherDao.getFavWeather()
    }

    override fun getFavWeatherWithId(entryId: String): Flow<FavWeatherEntity> {
        return _weatherDao.getFavWeatherWithId(entryId)
    }

    override fun insertIntoFav(favWeatherEntity: FavWeatherEntity) {
        _weatherDao.insertIntoFav(favWeatherEntity)
    }

    override suspend fun updateFavItemWithCurrentWeather(weather: FavWeatherEntity) {
        _weatherDao.updateFavItemWithCurrentWeather(weather)
    }

    override suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity) {
        _weatherDao.removeFromFav(favWeatherEntity)
    }

    override suspend fun insertIntoAlert(alertEntity: AlertEntity) {
        _weatherDao.insertIntoAlert(alertEntity)
    }

    override suspend fun removeFromAlerts(alertEntity: AlertEntity) {
        _weatherDao.removeFromAlerts(alertEntity)
    }

    override fun getAlerts(): Flow<List<AlertEntity>> {
        return _weatherDao.getAlerts()
    }

    override suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double) {
        withContext(Dispatchers.IO){
            _weatherDao.updateAlertItemLatLongById(entryId, lat, long)
        }

    }

    override fun getAlertWithId(entryId: String): AlertEntity {
            return _weatherDao.getAlertWithId(entryId)
    }


    companion object {
        private lateinit var instance: DefaultLocalDataSource
        fun getInstance(dao: WeatherDao): DefaultLocalDataSource {
            if (!::instance.isInitialized) {
                instance = DefaultLocalDataSource(dao)
            }
            return instance
        }
    }
}