package com.mad.iti.weather.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mad.iti.weather.model.weather.Current
import com.mad.iti.weather.model.weather.Daily
import com.mad.iti.weather.model.weather.Hourly
import java.util.*

@Entity(tableName = "FavWeatherData")
data class FavWeatherEntity (
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString(),
    val currentTime :String,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
)