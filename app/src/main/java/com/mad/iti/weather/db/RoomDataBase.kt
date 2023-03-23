package com.mad.iti.weather.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mad.iti.weather.model.weather.OneCallWeatherResponse

@Database(entities = [OneCallWeatherResponse::class], version = 1, exportSchema = false)
@TypeConverters(WeatherTypeConverter::class)
abstract class WeatherDataBase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}

@Volatile
private lateinit var INSTANCE: WeatherDataBase

fun getDatabase(context: Context): WeatherDataBase {
    synchronized(WeatherDataBase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext, WeatherDataBase::class.java, "Weather"
            ).build()
        }
    }
    return INSTANCE
}