package com.mad.iti.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.AlertKind
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.model.weather.Current
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TaskDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: WeatherDataBase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), WeatherDataBase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    //
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
        database.weatherDao.insertIntoFav(favWeatherEntity)

        //when - Get the task by id from database

        val loaded = database.weatherDao.getFavWeatherWithId(entryId = favWeatherEntity.id)

        //then
        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.id, CoreMatchers.`is`(favWeatherEntity.id))
                Assert.assertThat(it.lat, CoreMatchers.`is`(favWeatherEntity.lat))
                Assert.assertThat(
                    it.currentTime, CoreMatchers.`is`(favWeatherEntity.currentTime)
                )
                Assert.assertThat(it.lon, CoreMatchers.`is`(favWeatherEntity.lon))
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

        database.weatherDao.insertIntoFav(favWeatherEntity)

        //when - update the task
        val favWeatherEntity1 = favWeatherEntity.copy(currentTime = "title2")
        database.weatherDao.updateFavItemWithCurrentWeather(favWeatherEntity1)
        val loaded = database.weatherDao.getFavWeatherWithId(favWeatherEntity.id)

        //then

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.id, CoreMatchers.`is`(favWeatherEntity1.id))
                Assert.assertThat(it.lat, CoreMatchers.`is`(favWeatherEntity1.lat))
                Assert.assertThat(
                    it.currentTime, CoreMatchers.`is`(favWeatherEntity1.currentTime)
                )
                Assert.assertThat(it.lon, CoreMatchers.`is`(favWeatherEntity1.lon))
                cancel()
            }

        }
    }

    @Test
    fun getAllFavWithoutInsertingReturnEmpty() = runTest {
        //Given - Insert non


        //when - return all fav
        val loaded = database.weatherDao.getFavWeather()

        //then

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it, CoreMatchers.`is`(emptyList()))

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

        database.weatherDao.insertIntoFav(favWeatherEntity1)
        database.weatherDao.insertIntoFav(favWeatherEntity2)
        //when - return all fav
        val loaded = database.weatherDao.getFavWeather()

        //then

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(2))

                cancel()
            }

        }

    }


    @Test
    fun deleteAllFavWithInsertingTwoFavReturnEmptyList() = runTest {
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

        database.weatherDao.insertIntoFav(favWeatherEntity1)
        database.weatherDao.insertIntoFav(favWeatherEntity2)
        //when - return all fav
        database.weatherDao.deleteAllFromFav()


        //then
        val loaded = database.weatherDao.getFavWeather()
        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it, CoreMatchers.`is`(emptyList()))

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

        database.weatherDao.insertIntoFav(favWeatherEntity1)
        database.weatherDao.insertIntoFav(favWeatherEntity2)
        //when - return all fav
        database.weatherDao.removeFromFav(favWeatherEntity1)


        //then
        val loaded = database.weatherDao.getFavWeather()
        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(1))

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
        database.weatherDao.insertIntoAlert(alertEntity)

        //when - Get the task by id from database

        val loaded = database.weatherDao.getAlertWithId(entryId = alertEntity.id)

        //then

        Assert.assertThat(loaded, CoreMatchers.notNullValue())
        Assert.assertThat(loaded.id, CoreMatchers.`is`(alertEntity.id))
        Assert.assertThat(loaded.lat, CoreMatchers.`is`(alertEntity.lat))
        Assert.assertThat(
            loaded.kind, CoreMatchers.`is`(alertEntity.kind)
        )
        Assert.assertThat(loaded.lon, CoreMatchers.`is`(alertEntity.lon))


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

        database.weatherDao.insertIntoAlert(alertEntity)

        //when - update the task
        database.weatherDao.updateAlertItemLatLongById(alertEntity.id, 10.0, 10.0)
        val loaded = database.weatherDao.getAlertWithId(alertEntity.id)

        //then
        Assert.assertThat(loaded, CoreMatchers.notNullValue())
        Assert.assertThat(loaded.lat, CoreMatchers.`is`(10.0))
        Assert.assertThat(loaded.lon, CoreMatchers.`is`(10.0))


    }

    @Test
    fun getAllAlertsWithoutInsertingReturnEmpty() = runTest {
        //Given - Insert non


        //when - return all alert
        val loaded = database.weatherDao.getAlerts()

        //then

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it, CoreMatchers.`is`(emptyList()))

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

        database.weatherDao.insertIntoAlert(alertEntity1)
        database.weatherDao.insertIntoAlert(alertEntity2)
        //when - return all alerts
        val loaded = database.weatherDao.getAlerts()

        //then

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(2))

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

        database.weatherDao.insertIntoAlert(alertEntity1)
        database.weatherDao.insertIntoAlert(alertEntity2)
        //when - return all fav
        database.weatherDao.removeFromAlerts(alertEntity2)


        //then
        val loaded = database.weatherDao.getAlerts()
        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(1))

                cancel()
            }

        }

    }


}