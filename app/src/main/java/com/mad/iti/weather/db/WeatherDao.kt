package com.mad.iti.weather.db

import androidx.room.*
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.entities.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("select * from WeatherData limit 1")
    fun getWeather(): Flow<WeatherEntity>


    @Query("delete from FavWeatherData")
    fun deleteAllFromFav()

    @Query("select * from FavWeatherData")
    fun getFavWeather(): Flow<List<FavWeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = FavWeatherEntity::class)
    fun insertIntoFav(weather: FavWeatherEntity)

    @Update(entity = FavWeatherEntity::class)
    fun updateFavItemWithCurrentWeather(weather: FavWeatherEntity)

    @Query("select * from FavWeatherData where entryid = :entryId limit 1")
    fun getFavWeatherWithId(entryId: String): Flow<FavWeatherEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = WeatherEntity::class)
    fun insert(weather: WeatherEntity)


    @Delete(entity = FavWeatherEntity::class)
    fun removeFromFav(favWeatherEntity: FavWeatherEntity)


    @Delete(entity = WeatherEntity::class)
    fun deleteThisWeather(weather: WeatherEntity)

    @Insert(entity = AlertEntity::class)
    fun insertIntoAlert(alertEntity: AlertEntity)


    @Delete(entity = AlertEntity::class)
    fun removeFromAlerts(alertEntity: AlertEntity)

    @Query("select * from AlertEntity")
    fun getAlerts(): Flow<List<AlertEntity>>

    @Query("select * from AlertEntity where entryid = :entryId limit 1")
    fun getAlertWithId(entryId: String): AlertEntity


    @Query("UPDATE AlertEntity SET lat = :lat, lon= :lon WHERE entryid = :entryId")
    fun updateAlertItemLatLongById(entryId: String, lat: Double, lon: Double)
}