package com.mad.iti.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mad.iti.weather.checkNetowrk.NetworkConnectivity
import com.mad.iti.weather.checkNetowrk.NetworkStatus
import com.mad.iti.weather.databinding.ActivityMainBinding
import com.mad.iti.weather.databinding.InitialSetupSettingDialogBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.language.changeLanguageLocaleTo
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.WeatherDataRepo
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences.Companion.NAVIGATE_TO_MAP
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences.Companion.SET_LOCATION_AS_MAIN_LOCATION
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
    private val settingPref by lazy {
        SettingSharedPreferences.getInstance(application)
    }
    private val mainViewModel: MainViewModel by lazy {
        factory = MainViewModel.Factory(
            _repo = WeatherDataRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(applicationContext).weatherDao)
            ), _loc = WeatherLocationManager.getInstance(application)
        )
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
    private var isConnected = true
    private val networkConnectivity by lazy {
        NetworkConnectivity.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
//        when(settingPref.getLocationPref()){
//            SettingSharedPreferences.MAP-> mainViewModel.getWeather("${location.latitude}", "${location.longitude}")
//        }
        mainViewModel.location.observe(this) { latLng ->
            mainViewModel.getWeather("${latLng.latitude}", "${latLng.longitude}")
        }


        val marginBottom = binding.navHostFragmentActivityMain.marginBottom
        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
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
        navView.setupWithNavController(navController)

    }

    override fun onStart() {
        super.onStart()
        if (getLanguageLocale().isBlank()) {
            changeLanguageLocaleTo(Locale.getDefault().language)
            Log.d(TAG, "onCreate: ")
        } else {
            when (settingPref.getLocationPref()) {
                SettingSharedPreferences.MAP -> {
                    mainViewModel.requestLocationUpdateSavedFromMap()
                }
                SettingSharedPreferences.GPS -> {
                    checkPermissionAndGetLoc()
                }
                else -> {
                    showInitialSetupDialog()
                }
            }
        }
    }


    private fun checkPermissionAndGetLoc() {
        if (checkPermission()) {
            Log.d(TAG, "onResume: ")
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, locationPermissions, MY_LOCATION_PERMISSION_ID
            )
        }
    }

    lateinit var alertDialog: AlertDialog
    private fun showInitialSetupDialog() {

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val initialSetupSettingDialogBinding: InitialSetupSettingDialogBinding =
            InitialSetupSettingDialogBinding.inflate(LayoutInflater.from(this), null, false)


        alertDialog = materialAlertDialogBuilder.setView(initialSetupSettingDialogBinding.root)
            .setTitle(getString(R.string.intial_setup)).setIcon(R.drawable.baseline_settings_24)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialogue_background, theme
                )
            ).setCancelable(false).show()

        initialSetupSettingDialogBinding.buttonSave.setOnClickListener {
            if (initialSetupSettingDialogBinding.radioMap.isChecked) {
                settingPref.setLocationPref(SettingSharedPreferences.MAP)
                openMapToSetLocation()
            } else {
                settingPref.setLocationPref(SettingSharedPreferences.GPS)
                checkPermissionAndGetLoc()
            }
            alertDialog.dismiss()

        }

        initialSetupSettingDialogBinding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun openMapToSetLocation() {
        with(Intent(this, MapsActivity::class.java)) {
            putExtra(NAVIGATE_TO_MAP, SET_LOCATION_AS_MAIN_LOCATION)
            startActivity(this)
        }
    }


    private fun getLastLocation() {
        Log.d(TAG, "getLastLocation: ")
        if (mainViewModel.isLocationEnabled()) {
            mainViewModel.requestLocationUpdateByGPS()
        } else {
            checkIsLocationEnabledDialog()
        }
    }


    private fun checkIsLocationEnabledDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.location_request))
            .setCancelable(false)
            .setMessage(getString(R.string.please_enable_loc))
            .setPositiveButton(
                getString(R.string.yes)
            ) { _, _ ->
                if (!mainViewModel.isLocationEnabled()) {
                    goToEnableTheLocation()
                }
            }.setNegativeButton(
                getString(R.string.no)
            ) { _, _ ->
                errorWarningForNotEnablingLocation()
                showSnackBarAskingHimToEnable()
            }.show()
    }

    private fun goToEnableTheLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun showSnackBarAskingHimToEnable() {
        val snackBar = Snackbar.make(
            binding.root,
            getString(R.string.please_enable_loc),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(getString(R.string.enable)) {
            if (!mainViewModel.isLocationEnabled()) {
                goToEnableTheLocation()
            }
            snackBar.dismiss()
        }.setBackgroundTint(getColor(R.color.textColor)).setTextColor(getColor(R.color.background))

        snackBar.setActionTextColor(getColor(R.color.background))
        snackBar.anchorView = binding.navView
        snackBar.show()
    }

    private fun errorWarningForNotEnablingLocation() {
        AlertDialog.Builder(this).setTitle(getString(R.string.warning)).setCancelable(false)
            .setMessage(
                getString(R.string.Unfortunately_the_location_is_disabled)
            ).setPositiveButton(android.R.string.ok) { _, _ -> }.show()
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

    override fun onStop() {
        super.onStop()
        if (::alertDialog.isInitialized) {
            alertDialog.dismiss()
        }

    }

}

fun FragmentContainerView.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    val params = layoutParams as MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}