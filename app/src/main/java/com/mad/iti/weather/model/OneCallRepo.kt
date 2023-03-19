package com.mad.iti.weather.model

import android.util.Log
import com.mad.iti.weather.network.APIClientInterface
import com.mad.iti.weather.networkUtils.APIStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "HomeFragment"

class OneCallRepo private constructor(private val apiClient: APIClientInterface) :
    OneCallRepoInterface {
    private val _weatherFlow = MutableStateFlow<APIStatus>(APIStatus.Loading)
    override val weatherFlow: StateFlow<APIStatus> = _weatherFlow

    override suspend fun enqueueWeatherCall(lat: String, lon: String) {
        runCatching {
            Log.d(TAG, "enqueueWeatherCall: ")
            val response = apiClient.getWeather(lat = lat, lon = lon)
            if (response.isSuccessful) {
                Log.d(TAG, "enqueueWeatherCall: ${response.isSuccessful}")
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    _weatherFlow.emit(APIStatus.Success(weatherResponse))
                } else {
                    _weatherFlow.emit(APIStatus.Failure("Response Body is NULL"))
                }
            } else {
                _weatherFlow.emit(APIStatus.Failure(response.errorBody().toString()))
            }
        }.onFailure {
            it.printStackTrace()
            _weatherFlow.emit(APIStatus.Failure(it.message ?: "Network Call Error"))
        }

    }

    companion object {
        private lateinit var instance: OneCallRepo
        fun getInstance(apiClient: APIClientInterface): OneCallRepo {
            if (!::instance.isInitialized) {
                instance = OneCallRepo(apiClient)
            }
            return instance
        }
    }
}

