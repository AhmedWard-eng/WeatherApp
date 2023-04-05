package com.mad.iti.weather.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng

private const val SETTING_SHARED_PREFERENCES = "SETTING_SHARED_PREFERENCES"
private const val LOCATION_PREFERENCES = "LOCATION_PREFERENCES"
private const val TEMP_PREFERENCES = "TEMP_PREFERENCES"
private const val WIND_SPEED_PREFERENCES = "WIND_SPEED_PREFERENCES"
private const val MAP_LAT_PREFERENCES = "MAP_LAT_PREFERENCES"
private const val MAP_LONG_PREFERENCES = "MAP_LONG_PREFERENCES"
class SettingSharedPreferences private constructor(applicationContext: Context) {
    private var sharedPreferences: SharedPreferences =
        applicationContext.getSharedPreferences(SETTING_SHARED_PREFERENCES, Context.MODE_PRIVATE)


    private val editor: SharedPreferences.Editor =
        sharedPreferences.edit()


    fun setLocationPref(locationPref: String) {
        editor.putString(LOCATION_PREFERENCES, locationPref).apply()
    }

    fun getLocationPref(): String? {
        return sharedPreferences.getString(LOCATION_PREFERENCES, "")
    }


    fun setTempPref(tempPref: String) {
        editor.putString(TEMP_PREFERENCES, tempPref).apply()
    }

    fun getTempPref(): String? {
        return sharedPreferences.getString(TEMP_PREFERENCES, CELSIUS)
    }



    fun setWindSpeedPref(windSpeedPref: String) {
        editor.putString(WIND_SPEED_PREFERENCES, windSpeedPref).apply()
    }

    fun getWindSpeedPref(): String? {
        return sharedPreferences.getString(WIND_SPEED_PREFERENCES, METER_PER_SECOND)
    }



    fun getMapPref(): LatLng{
        return LatLng(sharedPreferences.getFloat(MAP_LAT_PREFERENCES,0.0f).toDouble()
            ,sharedPreferences.getFloat(MAP_LONG_PREFERENCES,0.0f).toDouble())
    }

    fun setMapPref(latLng: LatLng){
        editor.putFloat(MAP_LAT_PREFERENCES,latLng.latitude.toFloat()).apply()
        editor.putFloat(MAP_LONG_PREFERENCES,latLng.longitude.toFloat()).apply()
    }

    companion object {
        const val GPS = "GPS"
        const val MAP = "MAP"

        const val METER_PER_SECOND = "METER_PER_SECOND"
        const val MILE_PER_HOUR = "MILE_PER_HOUR"

        const val CELSIUS = "CELSIUS"
        const val KELVIN = "KELVIN"
        const val FAHRENHEIT = "FAHRENHEIT"

        const val SET_LOCATION_AS_MAIN_LOCATION = "SET_LOCATION_AS_MAIN_LOCATION"
        const val ADD_T0_FAV_IN_THIS_LOCATION = "ADD_T0_FAV_IN_THIS_LOCATION"
        const val ADD_T0_ALERTS_IN_THIS_LOCATION = "ADD_T0_ALERTS_IN_THIS_LOCATION"
        const val NAVIGATE_TO_MAP = "NAVIGATE_TO_MAP"

        private lateinit var instance: SettingSharedPreferences

        fun getInstance(application: Context): SettingSharedPreferences {
            if (!::instance.isInitialized) {
                instance = SettingSharedPreferences(application.applicationContext)
            }
            return instance
        }
    }


}

