package com.mad.iti.weather.model

import android.util.Log
import com.mad.iti.weather.db.LocalDataSource
import com.mad.iti.weather.model.entities.WeatherData
import com.mad.iti.weather.network.APIClientInterface
import com.mad.iti.weather.utils.getWeatherDataFrom
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "HomeFragment"

class WeatherDataRepo private constructor(private val apiClient: APIClientInterface, private val localDataSource: LocalDataSource) :
    WeatherDataRepoInterface {
    private val _weatherFlow = MutableStateFlow<APIStatus>(APIStatus.Loading)
    override val weatherFlow: StateFlow<APIStatus> = _weatherFlow
    override suspend fun getWeatherData() : Flow<WeatherData> {
        return localDataSource.getWeatherData()
    }

    override suspend fun enqueueWeatherCall(lat: String, lon: String) {
        runCatching {
            Log.d(TAG, "enqueueWeatherCall: ")
            val response = apiClient.getWeather(lat = lat, lon = lon)
            if (response.isSuccessful) {
                Log.d(TAG, "enqueueWeatherCall: ${response.isSuccessful}")
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    _weatherFlow.emit(APIStatus.Success(getWeatherDataFrom(weatherResponse)))
                } else {
                    _weatherFlow.emit(APIStatus.Failure("Response Body is NULL"))
                }
            } else {
                Log.d(TAG, "enqueueWeatherCall: ${response.errorBody()}")
                _weatherFlow.emit(APIStatus.Failure(response.errorBody().toString()))
            }
        }.onFailure {
            it.printStackTrace()
            for(err in it.stackTrace) {
                Log.e(TAG, "enqueueWeatherCall: $err")
            }
            _weatherFlow.emit(APIStatus.Failure(it.message ?: "Network Call Error"))
        }
    }



    companion object {
        private lateinit var instance: WeatherDataRepo
        fun getInstance(apiClient: APIClientInterface,localDataSource: LocalDataSource): WeatherDataRepo {
            if (!::instance.isInitialized) {
                instance = WeatherDataRepo(apiClient,localDataSource)
            }
            return instance
        }
    }
}

