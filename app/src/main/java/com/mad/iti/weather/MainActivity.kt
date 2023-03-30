package com.mad.iti.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.mad.iti.weather.checkNetowrk.NetworkConnectivity
import com.mad.iti.weather.checkNetowrk.NetworkStatus
import com.mad.iti.weather.databinding.ActivityMainBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.language.changeLanguageLocaleTo
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.WeatherDataRepo
import com.mad.iti.weather.network.APIClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    private var isConnected = true
    private val networkConnectivity by lazy {
        NetworkConnectivity.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = MainViewModel.Factory(
            _repo = WeatherDataRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(applicationContext).weatherDao)
            ), _loc = WeatherLocationManager.getInstance(application)
        )
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]


        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        mainViewModel.location.observe(this) { location ->
            mainViewModel.getWeather("${location.latitude}", "${location.longitude}")
        }
        val marginBottom = binding.navHostFragmentActivityMain.marginBottom
        navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
            if (destination.id == R.id.showFavDetailsFragment) {
                navView.visibility = GONE
                binding.navHostFragmentActivityMain.setMargins(0, 0, 0, 0)
            } else {
                navView.visibility = VISIBLE

                binding.navHostFragmentActivityMain.setMargins(0, 0, 0, marginBottom)
            }
        }

        val noInternetSnackbar = Snackbar.make(
            binding.container,
            getString(R.string.internet_connection_is_lost),
            Snackbar.LENGTH_INDEFINITE
        )
        noInternetSnackbar.setAction("dismiss") {
            noInternetSnackbar.dismiss()
        }.setBackgroundTint(getColor(R.color.textColor)).setTextColor(getColor(R.color.background))

        noInternetSnackbar.setActionTextColor(getColor(R.color.background))
        noInternetSnackbar.anchorView = binding.navView
        if (getLanguageLocale().isBlank()) {
            changeLanguageLocaleTo(Locale.getDefault().language)
        }
        if (!networkConnectivity.isOnline()) {
            isConnected = false
            Log.d(TAG, "onCreate: ${networkConnectivity.isOnline()}")
            noInternetSnackbar.show()
        } else {
            isConnected = true
            noInternetSnackbar.dismiss()
        }

        val networkIsRestoredSnackbar = Snackbar.make(
            binding.container, getString(R.string.network_is_restored), Snackbar.LENGTH_SHORT
        )
        networkIsRestoredSnackbar.setAction("dismiss") {
            networkIsRestoredSnackbar.dismiss()
        }.setActionTextColor(getColor(R.color.background)).anchorView = binding.navView


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                networkConnectivity.connectivitySharedFlow.collectLatest {
                    when (it) {
                        NetworkStatus.LOST -> {
                            isConnected = false
                            networkIsRestoredSnackbar.dismiss()
                            noInternetSnackbar.show()
                        }
                        NetworkStatus.CONNECTED -> {
                            if (!isConnected) {
                                noInternetSnackbar.dismiss()
                                networkIsRestoredSnackbar.show()
                                isConnected = true
                            }
                        }
                    }
                }
            }
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

//        getDatabase(application).weatherDao.deleteAllFromFav()


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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }
}


fun FragmentContainerView.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    val params = layoutParams as MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}