package com.mad.iti.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.AlertKind
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.Current
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith




@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultLocalDataSourceTest{

    @get:Rule
    var instantExecutorRule= InstantTaskExecutorRule()

    private lateinit var localDataSource: DefaultLocalDataSource
    private lateinit var database: WeatherDataBase


    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDataBase::class.java
        ).allowMainThreadQueries()
            .build()

        localDataSource = DefaultLocalDataSource.getInstance(
            database.weatherDao
        )
    }


    @After
    fun closeDb() {
        database.close()
        database.clearAllTables()
    }


    @Test
    fun insertIntoFavAndGetById() = runBlockingTest {
        //Given - Insert a task.

        val favWeatherEntity = FavWeatherEntity(
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
        localDataSource.insertIntoFav(favWeatherEntity)

        //when - Get the task by id from database

        val loaded = localDataSource.getFavWeatherWithId(entryId = favWeatherEntity.id)

        //then
        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.id, CoreMatchers.`is`(favWeatherEntity.id))
                assertThat(it.lat, CoreMatchers.`is`(favWeatherEntity.lat))
                assertThat(
                    it.currentTime, CoreMatchers.`is`(favWeatherEntity.currentTime)
                )
                assertThat(it.lon, CoreMatchers.`is`(favWeatherEntity.lon))
                cancel()
            }

        }


    }

    //
    @Test
    fun updateFavAndGetById() = runTest {
        //Given - Insert a task.

        val favWeatherEntity = FavWeatherEntity(
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

        localDataSource.insertIntoFav(favWeatherEntity)

        //when - update the task
        val favWeatherEntity1 = favWeatherEntity.copy(currentTime = "title2")
        localDataSource.updateFavItemWithCurrentWeather(favWeatherEntity1)
        val loaded = localDataSource.getFavWeatherWithId(favWeatherEntity.id)

        //then

        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.id, CoreMatchers.`is`(favWeatherEntity1.id))
                assertThat(it.lat, CoreMatchers.`is`(favWeatherEntity1.lat))
                assertThat(
                    it.currentTime, CoreMatchers.`is`(favWeatherEntity1.currentTime)
                )
                assertThat(it.lon, CoreMatchers.`is`(favWeatherEntity1.lon))
                cancel()
            }

        }
    }

    @Test
    fun getAllFavWithoutInsertingReturnEmpty() = runTest {
        //Given - Insert non


        //when - return all fav
        val loaded = localDataSource.getWeatherFav()

        //then

        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it, CoreMatchers.`is`(emptyList()))

                cancel()
            }

        }
    }

    @Test
    fun getAllFavWithInsertingTwoFavReturnListSize2() = runTest {
        //Given - Insert 2 fav
        val favWeatherEntity1 = FavWeatherEntity(
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
        val favWeatherEntity2 = FavWeatherEntity(
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

        localDataSource.insertIntoFav(favWeatherEntity1)
        localDataSource.insertIntoFav(favWeatherEntity2)
        //when - return all fav
        val loaded = localDataSource.getWeatherFav()

        //then

        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.size, CoreMatchers.`is`(2))

                cancel()
            }

        }

    }

    @Test
    fun deleteFavWithInsertingTwoFavReturnListWithSize1() = runTest {
        //Given - Insert 2 fav
        val favWeatherEntity1 = FavWeatherEntity(
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
        val favWeatherEntity2 = FavWeatherEntity(
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

        localDataSource.insertIntoFav(favWeatherEntity1)
        localDataSource.insertIntoFav(favWeatherEntity2)
        //when - return all fav
        localDataSource.removeWeatherFromFav(favWeatherEntity1)


        //then
        val loaded = localDataSource.getWeatherFav()
        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.size, CoreMatchers.`is`(1))

                cancel()
            }

        }

    }


    //////////////////////////////////////////////////////


    @Test
    fun insertIntoAlertsAndGetById() = runTest {
        //Given - Insert an alert.

        val alertEntity = AlertEntity(
            start = 10L,
            end = 10L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )
        localDataSource.insertIntoAlert(alertEntity)

        //when - Get the task by id from database

        val loaded = localDataSource.getAlertWithId(entryId = alertEntity.id)

        //then

        assertThat(loaded, CoreMatchers.notNullValue())
        assertThat(loaded.id, CoreMatchers.`is`(alertEntity.id))
        assertThat(loaded.lat, CoreMatchers.`is`(alertEntity.lat))
        assertThat(
            loaded.kind, CoreMatchers.`is`(alertEntity.kind)
        )
        assertThat(loaded.lon, CoreMatchers.`is`(alertEntity.lon))


    }

    //
    @Test
    fun updateAlertItemLatLongWith1010AndGetById() = runTest {
        //Given - Insert a task.

        val alertEntity = AlertEntity(
            start = 10L,
            end = 10L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )

        localDataSource.insertIntoAlert(alertEntity)

        //when - update the task
        localDataSource.updateAlertItemLatLongById(alertEntity.id, 10.0, 10.0)
        val loaded = localDataSource.getAlertWithId(alertEntity.id)

        //then
        assertThat(loaded, CoreMatchers.notNullValue())
        assertThat(loaded.lat, CoreMatchers.`is`(10.0))
        assertThat(loaded.lon, CoreMatchers.`is`(10.0))


    }

    @Test
    fun getAllAlertsWithoutInsertingReturnEmpty() = runTest {
        //Given - Insert non


        //when - return all alert
        val loaded = localDataSource.getAlerts()

        //then

        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it, CoreMatchers.`is`(emptyList()))

                cancel()
            }

        }
    }

    @Test
    fun getAllAlertWithInsertingTwoAlertReturnListSize2() = runTest {
        //Given - Insert 2 alerts
        val alertEntity1 = AlertEntity(
            start = 10L,
            end = 10L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )
        val alertEntity2 = AlertEntity(
            start = 5L,
            end = 5L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )

        localDataSource.insertIntoAlert(alertEntity1)
        localDataSource.insertIntoAlert(alertEntity2)
        //when - return all alerts
        val loaded = localDataSource.getAlerts()

        //then

        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.size, CoreMatchers.`is`(2))

                cancel()
            }

        }
    }



    @Test
    fun deleteAlertWithInsertingTwoAlertsReturnListWithSize1() = runTest {
        //Given - Insert 2 alerts
        val alertEntity1 = AlertEntity(
            start = 10L,
            end = 10L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )
        val alertEntity2 = AlertEntity(
            start = 5L,
            end = 5L,
            kind = AlertKind.ALARM,
            lon = 3.0,
            lat = 3.0
        )

        localDataSource.insertIntoAlert(alertEntity1)
        localDataSource.insertIntoAlert(alertEntity2)
        //when - return all fav
        localDataSource.removeFromAlerts(alertEntity2)


        //then
        val loaded = localDataSource.getAlerts()
        launch {
            loaded.collectLatest {

                assertThat(it, CoreMatchers.notNullValue())
                assertThat(it.size, CoreMatchers.`is`(1))

                cancel()
            }

        }

    }
}