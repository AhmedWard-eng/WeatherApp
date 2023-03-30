package com.mad.iti.weather.location

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

private const val TAG = "WeatherLocationManager"

class WeatherLocationManager private constructor(private var application : Application) :
    WeatherLocationManagerInterface {

    private val _location = MutableStateFlow<LocationStatus>(LocationStatus.Loading)
    override val location = _location.asStateFlow()

    private lateinit var locationCallback: LocationCallback
    private val mFusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }
    private val mLocationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L).apply {
            setMinUpdateIntervalMillis(100)
        }.build()
    }

    @SuppressLint("MissingPermission")
    override fun requestLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.lastLocation?.let {
                  val isEmitted = _location.tryEmit(LocationStatus.Success(it))
                  Log.d(TAG, "onLocationResult: $isEmitted")
                }
                removeLocationUpdate()
            }
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, locationCallback, Looper.myLooper()
        )
    }

    override fun removeLocationUpdate() {
        if (::locationCallback.isInitialized) {
            mFusedLocationProviderClient.removeLocationUpdates(
                locationCallback
            )
        }
    }


    override fun isLocationEnabled(): Boolean {
        val weatherLocationManager: LocationManager =
            application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return weatherLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }




    companion object {
        private lateinit var instance: WeatherLocationManager
        fun getInstance(application: Application): WeatherLocationManager {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = WeatherLocationManager(application)
                }
                return instance
            }
        }
    }
}