package com.mad.iti.weather.db

import androidx.room.*
import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.model.entities.WeatherData
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("select * from WeatherData limit 1")
    fun getWeather(): Flow<WeatherData>


    @Query("delete from FavWeatherData")
    fun deleteAllFromFav()

    @Query("select * from FavWeatherData")
    fun getFavWeather(): Flow<List<FavWeatherData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = FavWeatherData::class)
    fun insertIntoFav(weather: FavWeatherData)

    @Update(entity = FavWeatherData::class)
    fun updateFavItemWithCurrentWeather(weather: FavWeatherData)

    @Query("select * from FavWeatherData where entryid = :entryId limit 1")
    fun getFavWeatherWithId(entryId: String): Flow<FavWeatherData>


    @Insert(onConflict = OnConflictStrategy.REPLACE,  entity = WeatherData::class)
    fun insert(weather: WeatherData)


    @Delete(entity = FavWeatherData::class)
    fun removeFromFav(favWeatherData: FavWeatherData)


    @Delete(entity = WeatherData::class)
    fun deleteThisWeather(weather: WeatherData)
}