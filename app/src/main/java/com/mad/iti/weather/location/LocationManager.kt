package com.mad.iti.weather.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationManager(var context: Context) {

    private lateinit var locationCallback: LocationCallback
    private val mFusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private val mLocationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L).apply {
            setMinUpdateIntervalMillis(100)
        }.build()
    }

    @SuppressLint("MissingPermission")
    fun requestLocation(callBack: (Location) -> Unit) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.lastLocation?.let { callBack(it) }
            }
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, locationCallback, Looper.myLooper()
        )

    }

    fun removeLocationUpdate() {
        if (::locationCallback.isInitialized) {
            mFusedLocationProviderClient.removeLocationUpdates(
                locationCallback
            )
        }
    }



    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}