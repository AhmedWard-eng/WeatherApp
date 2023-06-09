package com.mad.iti.weather.location

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


private const val TAG = "WeatherLocationManager"

class WeatherLocationManager private constructor(private var application: Application) :
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
    override fun requestLocationByGPS() {
        val tokenSource = CancellationTokenSource()
        val token = tokenSource.token
        mFusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,token).addOnSuccessListener {
            val isEmitted =
                _location.tryEmit(LocationStatus.Success(LatLng(it.latitude, it.longitude)))
            Log.d(TAG, "onLocationResult: $isEmitted")
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationByGPS(callback: (LatLng) -> Unit) {
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(p0: LocationResult) {
//                super.onLocationResult(p0)
//                p0.lastLocation?.let {
//                    callback(LatLng(it.latitude, it.longitude))
//                }
//
//            }
//        }
//        mFusedLocationProviderClient.requestLocationUpdates(
//            mLocationRequest, locationCallback, Looper.myLooper()
//        )

        val tokenSource = CancellationTokenSource()
        val token = tokenSource.token
        mFusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,token).addOnSuccessListener {
            callback(LatLng(it.latitude, it.longitude))
        }
    }



    override fun requestLocationSavedFromMap() {
        val isEmitted = _location.tryEmit(
            LocationStatus.Success(
                SettingSharedPreferences.getInstance(application).getMapPref()
            )
        )
        Log.d(TAG, "onLocationResult: $isEmitted")
    }

    override fun removeLocationUpdate(locationCallback: LocationCallback) {
        mFusedLocationProviderClient.removeLocationUpdates(
            locationCallback
        )
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