package com.mad.iti.weather.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mad.iti.weather.R
import com.mad.iti.weather.checkNetowrk.NetworkConnectivity
import com.mad.iti.weather.databinding.FragmentHomeBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.model.WeatherDataRepo
import com.mad.iti.weather.model.entities.WeatherEntity
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import com.mad.iti.weather.utils.locationUtils.formatAddressToCity
import com.mad.iti.weather.utils.locationUtils.getAddress
import com.mad.iti.weather.utils.statusUtils.APIStatus
import com.mad.iti.weather.utils.viewUtils.textView.setTemp
import com.mad.iti.weather.utils.viewUtils.textView.setTime
import com.mad.iti.weather.utils.viewUtils.textView.setWindSpeed
import com.mad.iti.weather.viewUtils.setImageFromWeatherIconId4x
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var requestPermission: ActivityResultLauncher<Array<String>?>
    private val homeViewModel: HomeViewModel by lazy {
        val factory = HomeViewModel.Factory(
            _repo = WeatherDataRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(requireActivity().application).weatherDao)
            ), WeatherLocationManager.getInstance(requireActivity().application)
        )
        ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }
    private val networkConnectivity by lazy {
        NetworkConnectivity.getInstance(requireActivity().application)
    }

    private lateinit var locationPermissions : Array<String>




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {




        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dailyAdapter = DailyAdapter(TimeZone.getDefault())
        hourlyAdapter = HourlyAdapter(TimeZone.getDefault())

        binding.recyclerView.adapter = hourlyAdapter

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // Respond to button selection
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonHourly -> binding.recyclerView.adapter = hourlyAdapter
                    R.id.buttonDaily -> binding.recyclerView.adapter = dailyAdapter
                }
            }
        }
        val noInternetSnackbar = Snackbar.make(
            binding.root, getString(R.string.internet_connection_is_lost), Snackbar.LENGTH_SHORT
        )
        noInternetSnackbar.setAction("dismiss") {
            noInternetSnackbar.dismiss()
        }.setBackgroundTint(requireContext().getColor(R.color.textColor))
            .setTextColor(requireContext().getColor(R.color.background))

        noInternetSnackbar.setActionTextColor(requireContext().getColor(R.color.background))
        binding.buttonRefresh.setOnClickListener {
            if (networkConnectivity.isOnline()) {
                val locStatus = homeViewModel.location.value
                if (locStatus is LocationStatus.Success) {
                    if (networkConnectivity.isOnline()) {
                        binding.NoInternet.visibility = GONE
                        binding.internet.visibility = VISIBLE
                        homeViewModel.getWeather(
                            locStatus.latLng.latitude, locStatus.latLng.longitude
                        )
                    } else {
                        binding.internet.visibility = GONE
                        binding.NoInternet.visibility = VISIBLE
                    }
                }
            } else {
                noInternetSnackbar.show()
            }
        }

//        val textView: TextView = binding.textHome
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.weather.collectLatest { status ->
                    Log.d(TAG, "onCreateView: ${status.javaClass}")
                    when (status) {
                        is APIStatus.Loading -> {
                            if (networkConnectivity.isOnline()) {
                                binding.internet.visibility = GONE
                                binding.NoInternet.visibility = GONE
                                binding.progressBarLoading.visibility = VISIBLE
                            }
                        }
                        is APIStatus.Success -> {
                            binding.progressBarLoading.visibility = GONE
                            binding.NoInternet.visibility = GONE
                            binding.internet.visibility = VISIBLE
                            Log.d(TAG, "onCreateView: Success")
                            setWeatherDataToTheView(status.weatherEntity)
                            getAddress(
                                requireContext(),
                                lat = status.weatherEntity.lat,
                                long = status.weatherEntity.lon,
                                locale = Locale(getLanguageLocale())
                            ) { address ->
                                Log.d(
                                    TAG, "getLanguageLocale: ${getLanguageLocale()}"
                                )
                                binding.txtViewLocation.text =
                                    address?.let { it1 -> formatAddressToCity(it1) }
                            }
                        }
                        else -> {
                            if (networkConnectivity.isOnline()) {
                                binding.askForPermission.visibility = GONE
                                binding.NoInternet.visibility = GONE
                                binding.internet.visibility = GONE
                                binding.progressBarLoading.visibility = VISIBLE

                                if(SettingSharedPreferences.getInstance(requireActivity().application).getLocationPref() == SettingSharedPreferences.GPS){
                                    Log.d(TAG, "onCreateViewTT: ${
                                        SettingSharedPreferences.getInstance(
                                            requireActivity().application
                                        ).getLocationPref()
                                    }")
                                    if(!checkPermission()){
                                        Log.d(TAG, "onCreateViewTT: ${
                                            checkPermission()
                                        }")
                                        binding.askForPermission.visibility = VISIBLE
                                        binding.NoInternet.visibility = GONE
                                        binding.internet.visibility = GONE
                                        binding.progressBarLoading.visibility = GONE
                                    }
                                }

                            } else {
                                Log.d(TAG, "onCreateView: ")
                                binding.progressBarLoading.visibility = GONE
                                binding.internet.visibility = GONE
                                binding.NoInternet.visibility = VISIBLE
                            }
                            Log.e(TAG, (status as APIStatus.Failure).throwable)
                        }
                    }
                }

            }
        }

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

       locationPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        )
        requestPermission =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // returns Map<String, Boolean> where String represents the
                // permission requested and boolean represents the
                // permission granted or not
                // iterate over each entry of map and take action needed for
                // each permission requested
                permissions.forEach { actionMap ->
                    when (actionMap.key) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> {
                            if (actionMap.value) {
                                // permission granted continue the normal
                                // workflow of app
                                if (homeViewModel.isLocationEnabled()) {
                                    homeViewModel.requestLocation()
                                } else {
                                    checkIsLocationEnabledDialog()
                                }
                                Log.i("DEBUG", "permission granted")
                            } else {
                                // if permission denied then check whether never
                                // ask again is selected or not by making use of
                                // !ActivityCompat.shouldShowRequest
                                // PermissionRationale(requireActivity(),
                                // Manifest.permission.CAMERA)
                                Log.i("DEBUG", "permission denied")
                            }
                        }
                    }
                }
            }
    }



    override fun onStart() {
        super.onStart()

        binding.buttonAllowLocation.setOnClickListener {
            requestPermission.launch(locationPermissions)
        }
    }
    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }


    private fun setWeatherDataToTheView(weatherEntity: WeatherEntity) {
        binding.txtViewTemperatureDegree.setTemp(
            weatherEntity.current.temp.roundToInt(), context = requireActivity().application
        )

        binding.txtViewWeatherCondition.text = weatherEntity.current.weather[0].description
        binding.txtViewPressure.text = buildString {
            append(weatherEntity.current.pressure.toString())
            append(getString(R.string.hpa))
        }

        binding.txtViewUV.text = weatherEntity.current.uvi.toString()
        binding.txtViewVisibility.text = buildString {
            append(weatherEntity.current.visibility.toString())
            append(getString(R.string.m))
        }
        binding.txtViewCloud.text = buildString {
            append(weatherEntity.current.clouds.toString())
            append(" %")
        }
        binding.txtViewHumidity.text = buildString {
            append(weatherEntity.current.humidity.toString())
            append(" %")
        }
        binding.txtViewWind.setWindSpeed(
            weatherEntity.current.wind_speed, requireActivity().application
        )
        binding.imageViewWeatherIcon.setImageFromWeatherIconId4x(weatherEntity.current.weather[0].icon)
        binding.txtViewSunsetTime.setTime(weatherEntity.current.sunset, TimeZone.getTimeZone(weatherEntity.timezone))
        binding.txtViewSunriseTime.setTime(weatherEntity.current.sunrise, TimeZone.getTimeZone(weatherEntity.timezone))
        binding.txtViewCurrentTime.setTime(weatherEntity.current.dt, TimeZone.getTimeZone(weatherEntity.timezone))
        binding.motionLayout.progress = getProgress(
            weatherEntity.current.sunset, weatherEntity.current.sunrise
        )
        hourlyAdapter.submitList(weatherEntity.hourly)
        dailyAdapter.submitList(weatherEntity.daily)

    }

    private fun getProgress(sunset: Int, sunrise: Int): Float {
        val totalTime = (sunset - sunrise) * 1000.0f
        val timePassedFromSunRise = (System.currentTimeMillis() - (sunrise * 1000L)) * 1.0f
        return if (checkIfTheCurrentTimeLessThanSunriseTime(timePassedFromSunRise, totalTime)) 0.0f
        else if (checkIfTheCurrentTimeGreaterThanSunsetTime(timePassedFromSunRise, totalTime)) 1.0f
        else timePassedFromSunRise / totalTime

    }

    private fun checkIfTheCurrentTimeGreaterThanSunsetTime(
        timePassedFromSunRise: Float, totalTime: Float
    ) = timePassedFromSunRise / totalTime > 1

    private fun checkIfTheCurrentTimeLessThanSunriseTime(
        timePassedFromSunRise: Float, totalTime: Float
    ) = timePassedFromSunRise / totalTime < 0


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkIsLocationEnabledDialog() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.location_request))
            .setCancelable(false)
            .setMessage(getString(R.string.please_enable_loc))
            .setPositiveButton(
                getString(R.string.yes)
            ) { _, _ ->
                if (!homeViewModel.isLocationEnabled()) {
                    goToEnableTheLocation()
                }
            }.setNegativeButton(
                getString(R.string.no)
            ) { _, _ ->
                errorWarningForNotEnablingLocation()
                showSnackBarAskingHimToEnable()
            }.show()
    }
    private fun showSnackBarAskingHimToEnable() {
        val snackBar = Snackbar.make(
            binding.root,
            getString(R.string.please_enable_loc),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(getString(R.string.enable)) {
            if (!homeViewModel.isLocationEnabled()) {
                goToEnableTheLocation()
            }
            snackBar.dismiss()
        }.setBackgroundTint(requireActivity().getColor(R.color.textColor)).setTextColor(requireActivity().getColor(R.color.background))
        snackBar.setActionTextColor(requireActivity().getColor(R.color.background))
        snackBar.show()
    }

    private fun errorWarningForNotEnablingLocation() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning)).setCancelable(false)
            .setMessage(
                getString(R.string.Unfortunately_the_location_is_disabled)
            ).setPositiveButton(android.R.string.ok) { _, _ -> }.show()
    }

    private fun goToEnableTheLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
}







