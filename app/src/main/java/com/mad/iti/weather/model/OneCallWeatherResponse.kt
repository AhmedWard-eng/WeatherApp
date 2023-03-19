package com.mad.iti.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OneCallWeather")
data class OneCallWeatherResponse(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int
)