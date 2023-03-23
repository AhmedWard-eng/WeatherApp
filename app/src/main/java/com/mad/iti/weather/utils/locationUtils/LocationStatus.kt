package com.mad.iti.weather.utils.locationUtils

import android.location.Location

sealed class LocationStatus {
    class Success(var location: Location) : LocationStatus()

    class Failure(var throwable: String) : LocationStatus()

    object Loading : LocationStatus()
}
