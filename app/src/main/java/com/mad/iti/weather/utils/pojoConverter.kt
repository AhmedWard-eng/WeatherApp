package com.mad.iti.weather.utils

import android.util.Log
import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.model.entities.WeatherData
import com.mad.iti.weather.model.weather.OneCallWeatherResponse

private const val TAG = "pojoConverter"
fun getWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse): WeatherData {
    val date: String = getCurrentDateFormat()
    Log.d(TAG, "getWeatherDataFrom: $oneWeatherCall")
    return WeatherData(
        currentTime = date,
        current = oneWeatherCall.current,
        daily = oneWeatherCall.daily,
        hourly = oneWeatherCall.hourly,
        lat = oneWeatherCall.lat,
        lon = oneWeatherCall.lon,
        timezone = oneWeatherCall.timezone,
        timezone_offset = oneWeatherCall.timezone_offset
    )
}

fun getFavWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse): FavWeatherData {
    val date: String = getCurrentDateFormat()

    return FavWeatherData(
        currentTime = date,
        current = oneWeatherCall.current,
        daily = oneWeatherCall.daily,
        hourly = oneWeatherCall.hourly,
        lat = oneWeatherCall.lat,
        lon = oneWeatherCall.lon,
        timezone = oneWeatherCall.timezone,
        timezone_offset = oneWeatherCall.timezone_offset
    )
}
fun getFavWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse,id : String): FavWeatherData {
    val date: String = getCurrentDateFormat()

    return FavWeatherData(
        id = id,
        currentTime = date,
        current = oneWeatherCall.current,
        daily = oneWeatherCall.daily,
        hourly = oneWeatherCall.hourly,
        lat = oneWeatherCall.lat,
        lon = oneWeatherCall.lon,
        timezone = oneWeatherCall.timezone,
        timezone_offset = oneWeatherCall.timezone_offset
    )
}

