package com.mad.iti.weather.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.mad.iti.weather.model.*


class WeatherTypeConverter {
    @TypeConverter
    fun fromCurrentToString(current: Current): String = Gson().toJson(current)


    @TypeConverter
    fun fromStringToCurrent(string: String): Current = Gson().fromJson(string, Current::class.java)


    @TypeConverter
    fun fromListDailyToString(dailyItems: List<Daily>): String = Gson().toJson(dailyItems)


    @TypeConverter
    fun fromStringToListDaily(value: String): List<Daily> =
        Gson().fromJson(value, Array<Daily>::class.java).toList()


    @TypeConverter
    fun fromListHourlyToString(hourlyItems: List<Hourly>): String = Gson().toJson(hourlyItems)


    @TypeConverter
    fun fromStringToListHourly(value: String): List<Hourly> =
        Gson().fromJson(value, Array<Hourly>::class.java).toList()

    @TypeConverter
    fun fromListWeatherToString(weatherItems: List<Weather>): String = Gson().toJson(weatherItems)


    @TypeConverter
    fun fromStringToListWeather(value: String): List<Weather> =
        Gson().fromJson(value, Array<Weather>::class.java).toList()

    @TypeConverter
    fun fromFeelsLikeToString(feelsLike: FeelsLike): String = Gson().toJson(feelsLike)


    @TypeConverter
    fun fromStringToFeelsLike(string: String): FeelsLike =
        Gson().fromJson(string, FeelsLike::class.java)

    @TypeConverter
    fun fromTempToString(temp: Temp): String = Gson().toJson(temp)


    @TypeConverter
    fun fromStringToTemp(string: String): Temp = Gson().fromJson(string, Temp::class.java)

}