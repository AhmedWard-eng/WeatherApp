package com.mad.iti.weather.model

import com.mad.iti.weather.db.LocalDataSource
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.network.APIClientInterface
import com.mad.iti.weather.utils.getFavWeatherDataFrom
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import com.mad.iti.weather.utils.statusUtils.AlertsAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import retrofit2.Response


class FavAlertsWeatherRepo private constructor(
    private val apiClient: APIClientInterface,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : FavAlertsWeatherRepoInterface {

    private val _favWeatherListFlow = MutableStateFlow<FavListAPiStatus>(FavListAPiStatus.Loading)
    override val favWeatherListFlow: StateFlow<FavListAPiStatus> = _favWeatherListFlow


    private val _favAddingWeatherFlow =
        MutableStateFlow<AddingFavAPIStatus>(AddingFavAPIStatus.Loading)
    override val favAddingWeatherFlow: StateFlow<AddingFavAPIStatus> = _favAddingWeatherFlow


    private val _favWeatherFlow = MutableStateFlow<FavAPIStatus>(FavAPIStatus.Loading)
    override val favWeatherFlow: StateFlow<FavAPIStatus> = _favWeatherFlow


    private val _alertsWeatherFlow = MutableStateFlow<AlertsAPIStatus>(AlertsAPIStatus.Loading)
    override val alertsWeatherFlow: StateFlow<AlertsAPIStatus> = _alertsWeatherFlow

    override suspend fun saveWeatherIntoFav(lat: Double, long: Double) {
        withContext(ioDispatcher) {
            runCatching {
                val response = apiClient.getWeather(lat = "$lat", lon = "$long")
                if (response.isSuccessful) {
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

    }

    override suspend fun insertIntoAlerts(alertEntity: AlertEntity) {
        withContext(ioDispatcher) {
            localDataSource.insertIntoAlert(alertEntity)
        }
    }

    override suspend fun removeFromAlerts(alertEntity: AlertEntity) {
        withContext(ioDispatcher) {
            localDataSource.removeFromAlerts(alertEntity)
        }
    }

    override suspend fun getAlerts() {
        localDataSource.getAlerts().catch {
            _alertsWeatherFlow.emit(AlertsAPIStatus.Failure(it.message ?: "Error"))
        }.collectLatest {
            _alertsWeatherFlow.emit(AlertsAPIStatus.Success(it))
        }

    }


    override suspend fun updateWeatherFavInfo(favWeatherEntity: FavWeatherEntity) {
        withContext(ioDispatcher) {
            runCatching {
                val response = apiClient.getWeather(
                    lat = "${favWeatherEntity.lat}", lon = "${favWeatherEntity.lon}"
                )
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        localDataSource.updateFavItemWithCurrentWeather(
                            getFavWeatherDataFrom(
                                weatherResponse, favWeatherEntity.id
                            )
                        )
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override suspend fun updateFlowWithCurrentData(id: String) {
        withContext(ioDispatcher) {
            localDataSource.getFavWeatherWithId(id).catch {
                _favWeatherFlow.emit(FavAPIStatus.Failure(it.message ?: "Error"))
            }.collectLatest {
                _favWeatherFlow.emit(FavAPIStatus.Success(it))
            }
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


    override suspend fun removeWeatherFromFav(favWeatherEntity: FavWeatherEntity) {
        withContext(ioDispatcher) {
            localDataSource.removeWeatherFromFav(favWeatherEntity)
        }
    }

    override suspend fun updateAlertItemLatLongById(entryId: String, lat: Double, long: Double) {
        withContext(ioDispatcher) {
            localDataSource.updateAlertItemLatLongById(entryId, lat, long)
        }
    }

    override fun getAlertWithId(entryId: String): AlertEntity {
        return localDataSource.getAlertWithId(entryId)
    }

    override suspend fun getWeather(lat: String, lon: String): Response<OneCallWeatherResponse> {
        return apiClient.getWeather(lat = lat, lon = lon)
    }




    companion object {
        private lateinit var instance: FavAlertsWeatherRepo
        fun getInstance(
            apiClient: APIClientInterface,
            localDataSource: LocalDataSource,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): FavAlertsWeatherRepo {
            if (!::instance.isInitialized) {
                instance = FavAlertsWeatherRepo(apiClient, localDataSource,ioDispatcher)
            }
            return instance
        }
    }
}