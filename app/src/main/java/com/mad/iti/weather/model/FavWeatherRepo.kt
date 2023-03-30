package com.mad.iti.weather.model

import android.util.Log
import com.mad.iti.weather.db.LocalDataSource
import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.network.APIClientInterface
import com.mad.iti.weather.utils.getFavWeatherDataFrom
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "FavWeatherRepo"

class FavWeatherRepo private constructor(
    private val apiClient: APIClientInterface, private val localDataSource: LocalDataSource
) : FavWeatherRepoInterface {

    private val _favWeatherListFlow = MutableStateFlow<FavListAPiStatus>(FavListAPiStatus.Loading)
    override val favWeatherListFlow: StateFlow<FavListAPiStatus> = _favWeatherListFlow


    private val _favAddingWeatherFlow =
        MutableStateFlow<AddingFavAPIStatus>(AddingFavAPIStatus.Loading)
    override val favAddingWeatherFlow: StateFlow<AddingFavAPIStatus> = _favAddingWeatherFlow


    private val _favWeatherFlow = MutableStateFlow<FavAPIStatus>(FavAPIStatus.Loading)
    override val favWeatherFlow: StateFlow<FavAPIStatus> = _favWeatherFlow

    override suspend fun saveWeatherIntoFav(lat: Double, long: Double) {
        runCatching {
            Log.d(TAG, "enqueueWeatherCall: ")
            val response = apiClient.getWeather(lat = "$lat", lon = "$long")
            if (response.isSuccessful) {
                Log.d(TAG, "enqueueWeatherCall: ${response.isSuccessful}")
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    localDataSource.insertIntoFav(getFavWeatherDataFrom(weatherResponse))
                    _favAddingWeatherFlow.emit(AddingFavAPIStatus.Success)
                } else {
                    _favAddingWeatherFlow.emit(AddingFavAPIStatus.Failure("Response Body is NULL"))
                }
            } else {
                _favAddingWeatherFlow.emit(
                    AddingFavAPIStatus.Failure(
                        response.errorBody().toString()
                    )
                )
            }
        }.onFailure {
            it.printStackTrace()
            _favAddingWeatherFlow.emit(
                AddingFavAPIStatus.Failure(
                    it.message ?: "Network Call Error"
                )
            )
        }
    }


    override suspend fun updateWeatherFavInfo(favWeatherData: FavWeatherData) {
        runCatching {
            val response =
                apiClient.getWeather(lat = "${favWeatherData.lat}", lon = "${favWeatherData.lon}")
            if (response.isSuccessful) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    localDataSource.updateFavItemWithCurrentWeather(
                        getFavWeatherDataFrom(
                            weatherResponse,
                            favWeatherData.id
                        )
                    )
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    override suspend fun updateFlowWithCurrentData(id:String){
        localDataSource.getFavWeatherWithId(id).catch {
            _favWeatherFlow.emit(FavAPIStatus.Failure(it.message ?: "Error"))
        }.collectLatest {
            _favWeatherFlow.emit(FavAPIStatus.Success(it))
        }
    }

    override suspend fun getWeatherFavData() {
        localDataSource.getWeatherFav().catch {
            _favWeatherListFlow.emit(FavListAPiStatus.Failure(it.message ?: "Error"))
        }.collect {
            if (it != null) {
                _favWeatherListFlow.emit(FavListAPiStatus.Success(it))
            } else {
                _favWeatherListFlow.emit(FavListAPiStatus.Failure("Error"))
            }
        }
    }

    override suspend fun removeWeatherFromFav(favWeatherData: FavWeatherData) {
        localDataSource.removeWeatherFromFav(favWeatherData)
    }

    companion object {
        private lateinit var instance: FavWeatherRepo
        fun getInstance(
            apiClient: APIClientInterface,
            localDataSource: LocalDataSource
        ): FavWeatherRepo {
            if (!::instance.isInitialized) {
                instance = FavWeatherRepo(apiClient, localDataSource)
            }
            return instance
        }
    }
}