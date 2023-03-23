package com.mad.iti.weather.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mad.iti.weather.R
import com.mad.iti.weather.viewUtils.setImageFromWeatherIconId4x
import com.mad.iti.weather.databinding.FragmentHomeBinding
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import com.mad.iti.weather.utils.locationUtils.formatAddress
import com.mad.iti.weather.utils.locationUtils.getAddress
import com.mad.iti.weather.model.OneCallRepo
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.utils.networkUtils.APIStatus
import com.mad.iti.weather.utils.viewUtils.textView.setTime
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var hourlyAdapter: HourlyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        val factory = HomeViewModel.Factory(
            _repo = OneCallRepo.getInstance(APIClient),
            WeatherLocationManager.getInstance(requireActivity().application)
        )
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dailyAdapter = DailyAdapter()
        hourlyAdapter = HourlyAdapter()

        binding.recyclerView.adapter = hourlyAdapter

        binding.toggleGroup.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            // Respond to button selection
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonHourly -> binding.recyclerView.adapter = hourlyAdapter
                    else -> binding.recyclerView.adapter = dailyAdapter
                }
            }
        }

//        val textView: TextView = binding.textHome
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                homeViewModel.weather.collectLatest { status ->
                    Log.d(TAG, "onCreateView: ${status.javaClass}")
                    when (status) {
                        is APIStatus.Loading -> {
                            Log.d(TAG, "onCreateView: Loading")
                        }
                        is APIStatus.Success -> {
                            Log.d(TAG, "onCreateView: Success")
                            setWeatherDataToTheView(status.oneCallWeatherResponse)
                        }
                        else -> {
                            Log.e(TAG, (status as APIStatus.Failure).throwable)
                        }
                    }
                }

            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                homeViewModel.loc.collectLatest { status ->

                    when (status) {
                        is LocationStatus.Loading -> {
                            Log.d(TAG, "onCreateView: Loading")
                        }
                        is LocationStatus.Success -> {
                            Log.d(TAG, "onCreateView: ${getLanguageLocale()}")
                            getAddress(
                                requireContext(), status.location, Locale(getLanguageLocale())
                            ) { address ->
                                Log.d(
                                    TAG, "getLanguageLocale: ${getLanguageLocale()}"
                                )
                                binding.txtViewLocation.text =
                                    address?.let { it1 -> formatAddress(it1) }
                            }
                        }
                        else -> {
                            Log.e(TAG, (status as LocationStatus.Failure).throwable)
                        }
                    }
                }
            }
        }
        return root
    }

    private fun setWeatherDataToTheView(oneCallWeatherResponse: OneCallWeatherResponse) {
        binding.txtViewTemperatureDegree.text = oneCallWeatherResponse.current.temp.toString()
        binding.txtViewWeatherCondition.text = oneCallWeatherResponse.current.weather[0].description
        binding.txtViewPressure.text = oneCallWeatherResponse.current.pressure.toString()
        binding.txtViewUV.text = oneCallWeatherResponse.current.uvi.toString()
        binding.txtViewVisibility.text = oneCallWeatherResponse.current.visibility.toString()
        binding.txtViewCloud.text = oneCallWeatherResponse.current.clouds.toString()
        binding.txtViewHumidity.text = oneCallWeatherResponse.current.humidity.toString()
        binding.txtViewWind.text = oneCallWeatherResponse.current.wind_speed.toString()
        binding.imageViewWeatherIcon.setImageFromWeatherIconId4x(oneCallWeatherResponse.current.weather[0].icon)
        binding.txtViewSunsetTime.setTime(oneCallWeatherResponse.current.sunset)
        binding.txtViewSunriseTime.setTime(oneCallWeatherResponse.current.sunrise)
        binding.txtViewCurrentTime.setTime(System.currentTimeMillis())
        binding.motionLayout.progress = getProgress(
            oneCallWeatherResponse.current.sunset, oneCallWeatherResponse.current.sunrise
        )
        hourlyAdapter.submitList(oneCallWeatherResponse.hourly)
        dailyAdapter.submitList(oneCallWeatherResponse.daily)

    }

    private fun getProgress(sunset: Int, sunrise: Int): Float {
        val totalTime = (sunset - sunrise) * 1000.0f
        val timePassedFromSunRise = (System.currentTimeMillis() - (sunrise * 1000L)) * 1.0f
        return if (timePassedFromSunRise / totalTime < 0) 0.0f
        else if (timePassedFromSunRise / totalTime > 1) 1.0f
        else timePassedFromSunRise / totalTime

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



