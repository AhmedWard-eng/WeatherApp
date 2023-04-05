package com.mad.iti.weather.fake

import com.mad.iti.weather.model.WeatherDataRepoInterface
import com.mad.iti.weather.model.entities.WeatherEntity
import com.mad.iti.weather.model.weather.Current
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWeatherRepo() : WeatherDataRepoInterface {
    private val _weatherFlow = MutableStateFlow<APIStatus>(APIStatus.Loading)
    override val weatherFlow: StateFlow<APIStatus>
        get() {
            return _weatherFlow
        }

    override suspend fun refreshWeatherCall(lat: String, lon: String) {
//        Log.d("TAG", "enqueueWeatherCall: ")
        _weatherFlow.emit(
            APIStatus.Success(
                WeatherEntity(
                    currentTime = "0",
                    current = Current(
                        0, 0.0, 0, 0.0, 0, 0, 0, 0, 0.0, 0.0, 0, emptyList(), 0, 0.0, 0.0
                    ),
                    daily = emptyList(),
                    hourly = emptyList(),
                    lat = 0.0,
                    lon = 0.0,
                    timezone = "oneWeatherCall.timezone",
                    timezone_offset = 0
                )
            )
        )
    }

    override suspend fun getWeatherData() {
        TODO("Not yet implemented")
    }
}