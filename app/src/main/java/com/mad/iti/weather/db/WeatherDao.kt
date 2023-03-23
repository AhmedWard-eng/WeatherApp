package com.mad.iti.weather.db

import androidx.room.*
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("select * from OneCallWeather limit 1")
    fun getWeather(): Flow<List<OneCallWeatherResponse>>


    @Query("delete from OneCallWeather")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weather: OneCallWeatherResponse)

    @Delete
    fun deleteThisWeather(weather: OneCallWeatherResponse)
}