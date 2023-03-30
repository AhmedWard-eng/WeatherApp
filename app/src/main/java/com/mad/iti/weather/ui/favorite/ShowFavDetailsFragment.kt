package com.mad.iti.weather.ui.favorite

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
import com.mad.iti.weather.databinding.FragmentShowFavDetailsBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.model.FavWeatherRepo
import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.ui.home.DailyAdapter
import com.mad.iti.weather.ui.home.HourlyAdapter
import com.mad.iti.weather.utils.locationUtils.formatAddressToCity
import com.mad.iti.weather.utils.locationUtils.getAddress
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.viewUtils.textView.setTemp
import com.mad.iti.weather.utils.viewUtils.textView.setTime
import com.mad.iti.weather.utils.viewUtils.textView.setWindSpeed
import com.mad.iti.weather.viewUtils.setImageFromWeatherIconId4x
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "ShowFavDetailsFragment"

class ShowFavDetailsFragment : Fragment() {
    private lateinit var binding: FragmentShowFavDetailsBinding


    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var hourlyAdapter: HourlyAdapter

    private val viewModel: ShowFavDetailsViewModel by lazy {
        val factory = ShowFavDetailsViewModel.Factory(
            FavWeatherRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(requireActivity().application).weatherDao)
            )
        )
        ViewModelProvider(this, factory)[ShowFavDetailsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowFavDetailsBinding.inflate(layoutInflater, container, false)
        dailyAdapter = DailyAdapter(TimeZone.getDefault())
        hourlyAdapter = HourlyAdapter(TimeZone.getDefault())
        val id = arguments?.let { ShowFavDetailsFragmentArgs.fromBundle(it).id }
        viewModel.getWeather(id ?: "")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.weatherFlow.collect { status ->
                    Log.d(TAG, "onCreateView: ${status.javaClass}")
                    when (status) {
                        is FavAPIStatus.Loading -> {
                            Log.d(TAG, "onCreateView: Loading")
                        }
                        is FavAPIStatus.Success -> {
                            hourlyAdapter.timeZone =
                                TimeZone.getTimeZone(status.favWeatherData.timezone)
                            dailyAdapter.timeZone =
                                TimeZone.getTimeZone(status.favWeatherData.timezone)
                            Log.d(TAG, "onCreateView: Success")
                            setWeatherDataToTheView(status.favWeatherData)
                            getAddress(
                                requireContext(),
                                lat = status.favWeatherData.lat,
                                long = status.favWeatherData.lon,
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
                            Log.e(
                                TAG,
                                (status as FavAPIStatus.Failure).throwable
                            )
                        }
                    }

                }
            }
        }
    }


    private fun setWeatherDataToTheView(weatherData: FavWeatherData) {
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
        binding.txtViewSunsetTime.setTime(
            weatherData.current.sunset,
            TimeZone.getTimeZone(weatherData.timezone)
        )
        binding.txtViewSunriseTime.setTime(
            weatherData.current.sunrise,
            TimeZone.getTimeZone(weatherData.timezone)
        )
        binding.txtViewCurrentTime.setTime(
            weatherData.current.dt,
            TimeZone.getTimeZone(weatherData.timezone)
        )
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


}