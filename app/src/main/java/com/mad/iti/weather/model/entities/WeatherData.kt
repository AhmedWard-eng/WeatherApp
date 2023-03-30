package com.mad.iti.weather.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mad.iti.weather.model.weather.Current
import com.mad.iti.weather.model.weather.Daily
import com.mad.iti.weather.model.weather.Hourly

@Entity(tableName = "WeatherData")
data class WeatherData(
    @PrimaryKey val id: Int = 1,
    val currentTime :String,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
)

