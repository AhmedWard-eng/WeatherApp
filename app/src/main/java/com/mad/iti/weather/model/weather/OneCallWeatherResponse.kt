package com.mad.iti.weather.model.weather


data class OneCallWeatherResponse(
    val id : Int = 1,
    val alerts: List<Alert>,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,

    )