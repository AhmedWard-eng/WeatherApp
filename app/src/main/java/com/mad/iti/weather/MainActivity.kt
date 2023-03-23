package com.mad.iti.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mad.iti.weather.databinding.ActivityMainBinding
import com.mad.iti.weather.language.changeLanguageLocaleTo
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.OneCallRepo
import com.mad.iti.weather.network.APIClient
import java.util.*

private const val TAG = "MainActivity"
private const val MY_LOCATION_PERMISSION_ID = 5005

class MainActivity : AppCompatActivity() {
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: MainViewModel.Factory
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = MainViewModel.Factory(
            _repo = OneCallRepo.getInstance(APIClient),
            _loc = WeatherLocationManager.getInstance(application))
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]


        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        mainViewModel.location.observe(this){location->
            mainViewModel.getWeather("${location.latitude}", "${location.longitude}")
        }

        if(getLanguageLocale().isBlank()){
            changeLanguageLocaleTo(Locale.getDefault().language)
        }

        Log.d(TAG, "onCreate: ${getLanguageLocale()}")
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home,
//                R.id.navigation_favorites,
//                R.id.navigation_alarm,
//                R.id.navigation_setting
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)




//        Log.d(TAG, "onCreate: ${AppCompatDelegate.getApplicationLocales().toLanguageTags()}")
    }

    override fun onStart() {
        super.onStart()
        if (checkPermission()) {
            Log.d(TAG, "onResume: ")
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, MY_LOCATION_PERMISSION_ID)
        }
    }



    private fun getLastLocation() {
        Log.d(TAG, "getLastLocation: ")
        if (mainViewModel.isLocationEnabled()) {
            mainViewModel.requestLocationUpdate()
        } else {
            Toast.makeText(this, "please turn on location", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_LOCATION_PERMISSION_ID) {
            if (grantResults.isNotEmpty() &&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }
}