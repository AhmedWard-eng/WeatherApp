package com.mad.iti.weather.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
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
import com.mad.iti.weather.model.entities.WeatherData
import com.mad.iti.weather.network.APIClient
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
    private lateinit var homeViewModel: HomeViewModel
    private val networkConnectivity by lazy {
        NetworkConnectivity.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        val factory = HomeViewModel.Factory(
            _repo = WeatherDataRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(requireActivity().application).weatherDao)
            ), WeatherLocationManager.getInstance(requireActivity().application)
        )
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

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
                            locStatus.location.latitude, locStatus.location.longitude
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
        if (networkConnectivity.isOnline()) {
            binding.NoInternet.visibility = GONE
            binding.internet.visibility = GONE
            binding.progressBarLoading.visibility = VISIBLE
        } else {
            Log.d(TAG, "onCreateView: ")
            binding.progressBarLoading.visibility = GONE
            binding.internet.visibility = GONE
            binding.NoInternet.visibility = VISIBLE
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
                            setWeatherDataToTheView(status.weatherData)
                            getAddress(
                                requireContext(),
                                lat = status.weatherData.lat,
                                long = status.weatherData.lon,
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
                            Log.e(TAG, (status as APIStatus.Failure).throwable)
                        }
                    }
                }

            }
        }

        return root
    }


    private fun setWeatherDataToTheView(weatherData: WeatherData) {
        binding.txtViewTemperatureDegree.setTemp(
            weatherData.current.temp.roundToInt(), context = requireActivity().application
        )

        binding.txtViewWeatherCondition.text = weatherData.current.weather[0].description
        binding.txtViewPressure.text = buildString {
            append(weatherData.current.pressure.toString())
            append(getString(R.string.hpa))
        }

        binding.txtViewUV.text = weatherData.current.uvi.toString()
        binding.txtViewVisibility.text = buildString {
            append(weatherData.current.visibility.toString())
            append(getString(R.string.m))
        }
        binding.txtViewCloud.text = buildString {
            append(weatherData.current.clouds.toString())
            append(" %")
        }
        binding.txtViewHumidity.text = buildString {
            append(weatherData.current.humidity.toString())
            append(" %")
        }
        binding.txtViewWind.setWindSpeed(
            weatherData.current.wind_speed, requireActivity().application
        )
        binding.imageViewWeatherIcon.setImageFromWeatherIconId4x(weatherData.current.weather[0].icon)
        binding.txtViewSunsetTime.setTime(weatherData.current.sunset)
        binding.txtViewSunriseTime.setTime(weatherData.current.sunrise)
        binding.txtViewCurrentTime.setTime(System.currentTimeMillis())
        binding.motionLayout.progress = getProgress(
            weatherData.current.sunset, weatherData.current.sunrise
        )
        hourlyAdapter.submitList(weatherData.hourly)
        dailyAdapter.submitList(weatherData.daily)

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
}







