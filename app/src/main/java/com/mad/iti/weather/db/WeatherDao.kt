package com.mad.iti.weather.db

import androidx.room.*
import com.mad.iti.weather.model.OneCallWeatherResponse

@Dao
interface WeatherDao {
    @Query("select * from OneCallWeather")
    fun getWeather(): List<OneCallWeatherResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(weather: OneCallWeatherResponse)

    @Delete
    fun deleteThisWeather(weather: OneCallWeatherResponse)
}