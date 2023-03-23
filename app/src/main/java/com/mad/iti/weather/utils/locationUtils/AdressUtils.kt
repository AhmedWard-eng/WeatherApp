package com.mad.iti.weather.utils.locationUtils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import java.util.*

private const val TAG = "AdressUtils"
fun formatAddress(address: Address): String {
    return address.let {
        if (it.subAdminArea != null) {
            "${it.adminArea}, ${it.subAdminArea}"
        } else {
            it.adminArea
        }
    }
}

fun getAddress(context: Context, location: Location, locale: Locale, onResult: (Address?) -> Unit) {
    var address: Address?
    val geocoder = Geocoder(context, locale)


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocation(
            location.latitude, location.longitude, 1
        ) {
            address = it[0]
            onResult(address)
        }
    } else {
        address = geocoder.getFromLocation(location.latitude, location.longitude, 1)?.get(0)
        onResult(address)
    }
}