package com.mad.iti.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mad.iti.weather.databinding.ActivityMapsBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.FavWeatherRepo
import com.mad.iti.weather.model.WeatherDataRepo
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var marker: Marker? = null
    private lateinit var _latLng: LatLng

    private val viewModel by viewModels<MapsViewModel>() {
        MapsViewModel.Factory(
            WeatherDataRepo.getInstance(
                APIClient, DefaultLocalDataSource.getInstance(getDatabase(application).weatherDao)
            ), WeatherLocationManager.getInstance(application), FavWeatherRepo.getInstance(
                APIClient, DefaultLocalDataSource.getInstance(getDatabase(application).weatherDao)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


//        val autocompleteFragment =
//            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
//        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
//        packageManager
//            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
//            .apply {
//
//                val apiKey = metaData.getString("com.google.android.geo.API_KEY")
//                if (!Places.isInitialized()) {
//                    Log.d(TAG, "onCreate: Places.isInitialized() ${Places.isInitialized()}")
//                    if (apiKey != null) {
//                        Places.initialize(applicationContext, apiKey, Locale.US)
//                    }
//                }
//
//            }
        binding.buttonSave.setOnClickListener {
            if (::_latLng.isInitialized)
                viewModel.saveLocationToFav(latLng = _latLng)
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.isSaved.collect {
                        when (it) {
                            is AddingFavAPIStatus.Success -> {
                                binding.progressBar.visibility = GONE
                                finish()
                            }
                            is AddingFavAPIStatus.Failure -> {
                                binding.progressBar.visibility = GONE
                                Log.d(TAG, "onCreate: ${it.throwable}")
                            }
                            else -> binding.progressBar.visibility = VISIBLE
                        }
                    }
                }
            }
        }
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        if (isPermissionGranted()) {
            viewModel.requestLocation()
            moveCameraToMyLocation()
        }
        setMapLongClick(googleMap)
        setPoiClick(googleMap)
        semMapCameraChanged(googleMap)
        enableMyLocation()
    }

    private fun semMapCameraChanged(googleMap: GoogleMap) {
        googleMap.setOnCameraMoveListener {
            googleMap.clear()
            _latLng = googleMap.cameraPosition.target
            marker = googleMap.addMarker(MarkerOptions().position(_latLng))
            binding.buttonSave.visibility = VISIBLE
        }
    }

    private fun moveCameraToMyLocation() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.location.collect { locationStatus ->
                    if (locationStatus is LocationStatus.Success) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    locationStatus.location.latitude,
                                    locationStatus.location.longitude
                                ), 10.0f
                            )
                        )
                        Log.d(TAG, "moveCameraToMyLocation: ")
                        viewModel.removeLocationUpdate()
                    }
                }
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            marker?.remove()
            val snippet = String.format(
                Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude
            )
            val markerOptions = MarkerOptions().position(latLng).snippet(snippet)
            map.moveCamera(
                CameraUpdateFactory.newLatLng(
                    latLng
                )
            )
            _latLng = latLng
            binding.buttonSave.visibility = VISIBLE
            marker = map.addMarker(markerOptions)


        }
        map.setOnMapLongClickListener { latLng ->
        }

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )

            _latLng = poi.latLng
            binding.buttonSave.visibility = VISIBLE
            marker?.showInfoWindow()
        }
    }

    private fun isPermissionGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
//            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

}