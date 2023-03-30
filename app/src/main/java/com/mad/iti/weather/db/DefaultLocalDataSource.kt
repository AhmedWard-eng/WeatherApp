package com.mad.iti.weather.db

import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.model.entities.WeatherData
import kotlinx.coroutines.flow.Flow

class DefaultLocalDataSource private constructor(private val _weatherDao: WeatherDao) : LocalDataSource {
    override fun getWeatherData(): Flow<WeatherData> {
        return _weatherDao.getWeather()
    }

    override fun insertWeatherData(weatherData: WeatherData) {
        _weatherDao.insert(weatherData)
    }

    override fun getWeatherFav(): Flow<List<FavWeatherData>> {
        return _weatherDao.getFavWeather()
    }

    override fun getFavWeatherWithId(entryId: String): Flow<FavWeatherData>{
        return _weatherDao.getFavWeatherWithId(entryId)
    }

    override fun insertIntoFav(favWeatherData: FavWeatherData){
        _weatherDao.insertIntoFav(favWeatherData)
    }

    override fun updateFavItemWithCurrentWeather(weather: FavWeatherData) {
        _weatherDao.updateFavItemWithCurrentWeather(weather)
    }
    override suspend fun removeWeatherFromFav(favWeatherData: FavWeatherData){
        _weatherDao.removeFromFav(favWeatherData)
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