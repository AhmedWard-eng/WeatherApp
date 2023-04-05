package com.mad.iti.weather.model

import com.mad.iti.weather.fake.FakeSuccessApiClient
import com.mad.iti.weather.fake.FakeLocalDataSource
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.AlertKind
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.Current
import com.mad.iti.weather.model.weather.OneCallWeatherResponse
import com.mad.iti.weather.utils.statusUtils.AddingFavAPIStatus
import com.mad.iti.weather.utils.statusUtils.AlertsAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavAPIStatus
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FavAlertsWeatherRepoTest {

    private val favWeatherEntity1 = FavWeatherEntity(
        currentTime = "c1",
        current = Current(
            0, 0.0, 0, 0.0, 0, 0, 0, 0, 0.0, 0.0, 0, emptyList(), 0, 0.0, 0.0
        ),
        daily = emptyList(),
        hourly = emptyList(),
        lat = 3.0,
        lon = 3.0,
        timezone = "",
        timezone_offset = 1
    )
    private val favWeatherEntity2 = FavWeatherEntity(
        currentTime = "c1",
        current = Current(
            0, 0.0, 0, 0.0, 0, 0, 0, 0, 0.0, 0.0, 0, emptyList(), 0, 0.0, 0.0
        ),
        daily = emptyList(),
        hourly = emptyList(),
        lat = 3.0,
        lon = 3.0,
        timezone = "",
        timezone_offset = 1
    )


    private val alertEntity1 = AlertEntity(
        start = 10L, end = 10L, kind = AlertKind.ALARM, lon = 3.0, lat = 3.0
    )
    private val alertEntity2 = AlertEntity(
        start = 5L, end = 5L, kind = AlertKind.ALARM, lon = 3.0, lat = 3.0
    )

    private val oneCallWeatherResponse = OneCallWeatherResponse(
        current = Current(
            0, 0.0, 0, 0.0, 0, 0, 0, 0, 0.0, 0.0, 0, emptyList(), 0, 0.0, 0.0
        ),
        daily = emptyList(),
        hourly = emptyList(),
        lat = 0.0,
        lon = 0.0,
        timezone = "oneWeatherCall.timezone",
        timezone_offset = 0,
        alerts = emptyList()
    )

    private val alerts = mutableListOf(alertEntity1, alertEntity2)
    private val favWeathers = mutableListOf(favWeatherEntity1, favWeatherEntity2)

    private lateinit var fakeLocalDataSource: FakeLocalDataSource
    private lateinit var fakeSuccessApiClient: FakeSuccessApiClient

    private lateinit var repo: FavAlertsWeatherRepo


    @Before
    fun setUp() {
        fakeLocalDataSource = FakeLocalDataSource(alerts, favWeathers)
        fakeSuccessApiClient = FakeSuccessApiClient(oneCallWeatherResponse)
        repo = FavAlertsWeatherRepo.getInstance(
            fakeSuccessApiClient, fakeLocalDataSource, Dispatchers.Unconfined
        )
    }


    @ExperimentalCoroutinesApi
    @Test
    fun getWeatherFavData_saveANewWeatherIntoFav_returnListSized3() = runTest {
        //Given  save a new fav into database from network
        repo.saveWeatherIntoFav(10.0, 10.0)

        //when getWeatherFavData
        repo.getWeatherFavData()
        //then the size of the list will increase by one and be 3
        launch {
            repo.favWeatherListFlow.collect {
                assertThat(it is FavListAPiStatus.Success, CoreMatchers.`is`(true))
                it as FavListAPiStatus.Success
                assertThat(it.favWeatherEntityList.size, CoreMatchers.`is`(3))
                cancel()
            }
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun checkSuccessAdding_returnSuccess() = runTest {
        //Given  save a new fav into database from network
        repo.saveWeatherIntoFav(10.0, 10.0)

        //then the size of the list will increase by one and be 3
        launch {
            repo.favAddingWeatherFlow.collect {
                assertThat(it is AddingFavAPIStatus.Success, CoreMatchers.`is`(true))
                cancel()
            }
        }
    }



    //    suspend fun saveWeatherIntoFav(lat: Double, long: Double)
//
    @ExperimentalCoroutinesApi
    @Test
    fun updateWeatherFavInfo_withNetwork_ReturnTheNewWeatherData() = runTest {
        //given update with network
        repo.updateWeatherFavInfo(favWeatherEntity1)
        //when updateFlowWithCurrentData
        repo.updateFlowWithCurrentData(favWeatherEntity1.id)
        //then the updated time zone = oneWeatherCall.timezone
        launch {
            repo.favWeatherFlow.collect {
                assertThat(it is FavAPIStatus.Success, CoreMatchers.`is`(true))
                it as FavAPIStatus.Success
                assertThat(
                    it.favWeatherEntity.timezone, CoreMatchers.`is`("oneWeatherCall.timezone")
                )
                cancel()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun removeWeatherFromFav_returnLocalDataSourceListSizeEqual1() = runTest {

        //when remove and getWeatherFavData
        repo.removeWeatherFromFav(favWeatherEntity1)
        repo.getWeatherFavData()
        //then the size of the list will decrease by one and be 1

        launch {
            repo.favWeatherListFlow.collect {
                assertThat(it is FavListAPiStatus.Success, CoreMatchers.`is`(true))
                it as FavListAPiStatus.Success
                assertThat(it.favWeatherEntityList.size, CoreMatchers.`is`(1))
                cancel()
            }
        }

    }


    @ExperimentalCoroutinesApi
    @Test
    fun getAlerts_saveANewAlertIntoAlerts_returnListSized3() = runTest {
        val alertEntity = AlertEntity(
            start = 5L, end = 5L, kind = AlertKind.ALARM, lon = 3.0, lat = 3.0
        )
        //Given  save a new alert into database
        repo.insertIntoAlerts(alertEntity)

        //when get alertData
        repo.getAlerts()
        //then the size of the list will increase by one and be 3
        launch {
            repo.alertsWeatherFlow.collect {
                assertThat(it is AlertsAPIStatus.Success, CoreMatchers.`is`(true))
                it as AlertsAPIStatus.Success
                assertThat(it.alertEntityList.size, CoreMatchers.`is`(3))
                cancel()
            }
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun removeAlertFromAlerts_returnLocalDataSourceListSizeEqual1() = runTest {

        //when remove and getAlertData
        repo.removeFromAlerts(alertEntity1)
        repo.getAlerts()
        //then the size of the list will decrease by one and be 1

        launch {
            repo.alertsWeatherFlow.collect {
                assertThat(it is AlertsAPIStatus.Success, CoreMatchers.`is`(true))
                it as AlertsAPIStatus.Success
                assertThat(it.alertEntityList.size, CoreMatchers.`is`(1))
                cancel()
            }
        }

    }

    @ExperimentalCoroutinesApi
    @Test
    fun `updateAlertLatLng_with30,30_ReturnTheNewWeatherData`() = runTest {
        //given update AlertItem by 30.0,30.0
        repo.updateAlertItemLatLongById(alertEntity1.id,30.0,30.0)
        //when AlertItem by Id
        val alertEntity = repo.getAlertWithId(alertEntity1.id)
        //then the updated latLng will be 30.0 , 30.0


                assertThat(
                    alertEntity.lat, CoreMatchers.`is`(30.0)
                )

        assertThat(
            alertEntity.lon, CoreMatchers.`is`(30.0)
        )

    }

}