package com.mad.iti.weather.db

import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.model.entities.WeatherData
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWeatherData() : Flow<WeatherData>

    fun insertWeatherData(weatherData: WeatherData)

    fun getWeatherFav(): Flow<List<FavWeatherData>?>

    fun insertIntoFav(favWeatherData: FavWeatherData)

    fun updateFavItemWithCurrentWeather(weather: FavWeatherData)
    suspend fun removeWeatherFromFav(favWeatherData: FavWeatherData)
    fun getFavWeatherWithId(entryId: String): Flow<FavWeatherData>
}