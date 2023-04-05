package com.mad.iti.weather.model

import android.util.Log
import com.mad.iti.weather.db.LocalDataSource
import com.mad.iti.weather.network.APIClientInterface
import com.mad.iti.weather.utils.getWeatherDataFrom
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

private const val TAG = "HomeFragment"

class WeatherDataRepo private constructor(
    private val apiClient: APIClientInterface,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : WeatherDataRepoInterface {
    private val _weatherFlow = MutableStateFlow<APIStatus>(APIStatus.Loading)
    override val weatherFlow: StateFlow<APIStatus> = _weatherFlow
    override suspend fun getWeatherData() {
        localDataSource.getWeatherData().catch {
            _weatherFlow.emit(APIStatus.Failure(it.message ?: "error"))
        }.collectLatest {
            if(it == null){
                _weatherFlow.emit(APIStatus.Failure("Nothing in the database"))
            }else {
                _weatherFlow.emit(APIStatus.Success(it))
            }
        }
    }

    override suspend fun refreshWeatherCall(lat: String, lon: String) {
        withContext(ioDispatcher) {
            runCatching {
                val response = apiClient.getWeather(lat = lat, lon = lon)
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        localDataSource.insertWeatherData(
                            weatherEntity = getWeatherDataFrom(
                                weatherResponse
                            )
                        )
                    }
                }
            }.onFailure {
                it.printStackTrace()
                for (err in it.stackTrace) {
                    Log.e(TAG, "enqueueWeatherCall: $err")
                }
            }
        }

    }


    companion object {
        private lateinit var instance: WeatherDataRepo
        fun getInstance(
            apiClient: APIClientInterface,
            localDataSource: LocalDataSource,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): WeatherDataRepo {
            if (!::instance.isInitialized) {
                instance = WeatherDataRepo(apiClient, localDataSource, ioDispatcher)
            }
            return instance
        }
    }
}

