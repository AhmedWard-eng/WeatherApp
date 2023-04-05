package com.mad.iti.weather.utils.viewUtils.textView

import android.app.Application
import android.content.Context
import android.widget.TextView
import com.mad.iti.weather.R
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.utils.getADateFormat
import com.mad.iti.weather.utils.getDayFormat
import com.mad.iti.weather.utils.getTimeFormat
import java.util.TimeZone
import kotlin.math.roundToInt


fun TextView.setTime(timeInSecond: Int,timeZone: TimeZone) {
    text = getTimeFormat(timeInSecond * 1000L,timeZone)
}
fun TextView.setTime(timeInMilliSecond: Long) {
    text = getTimeFormat(timeInMilliSecond)
}


fun TextView.setDay(timeInSecond: Int,timeZone: TimeZone) {
    setText(getDayFormat(timeInSecond * 1000L,timeZone))
}



fun TextView.setWindSpeed(windSpeed: Double, application: Application) {
    val preferences = SettingSharedPreferences.getInstance(application)
    when (preferences.getWindSpeedPref()) {
        SettingSharedPreferences.METER_PER_SECOND -> text =
            buildString {
                append(windSpeed.roundToInt().toString())
                append(application.getString(R.string.meter_per_second))
            }
        SettingSharedPreferences.MILE_PER_HOUR -> text = buildString {
            append(getWindSpeedInMilesPerHour(windSpeed).roundToInt().toString())
            append(application.getString(R.string.mile_per_hour))
        }
    }
}

fun getWindSpeedInMilesPerHour(windSpeed: Double): Double {
    return windSpeed * 2.23694
}

fun TextView.setTemp(temp: Int, context: Context) {
    val symbol: String
    val preferences = SettingSharedPreferences.getInstance(context)
    val theTemp = when (preferences.getTempPref()) {
        SettingSharedPreferences.CELSIUS -> {
            symbol = context.getString(R.string.c_symbol)
            temp
        }
        SettingSharedPreferences.FAHRENHEIT -> {
            symbol = context.getString(R.string.f_symbol)
            (((temp) * 9.0 / 5) + 32).roundToInt()
        }
        SettingSharedPreferences.KELVIN -> {
            symbol = context.getString(R.string.k_symbol)
            (temp) + 273
        }
        else -> {
            symbol = context.getString(R.string.c_symbol)
            temp
        }
    }
    text = buildString {
        append(theTemp)
        append(" ")
        append(symbol)
    }
}

fun TextView.setDate(timeInMilliSecond: Long){
    text = getADateFormat(timeInMilliSecond)
}

