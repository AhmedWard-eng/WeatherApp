package com.mad.iti.weather.utils

import android.util.Log
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.entities.WeatherEntity
import com.mad.iti.weather.model.weather.OneCallWeatherResponse

private const val TAG = "pojoConverter"
fun getWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse): WeatherEntity {
    val date: String = getCurrentDateFormat()
    Log.d(TAG, "getWeatherDataFrom: $oneWeatherCall")
    return WeatherEntity(
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

fun getFavWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse): FavWeatherEntity {
    val date: String = getCurrentDateFormat()

    return FavWeatherEntity(
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
fun getFavWeatherDataFrom(oneWeatherCall: OneCallWeatherResponse,id : String): FavWeatherEntity {
    val date: String = getCurrentDateFormat()

    return FavWeatherEntity(
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

