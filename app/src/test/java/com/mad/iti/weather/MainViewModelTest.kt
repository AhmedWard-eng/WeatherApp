package com.mad.iti.weather

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mad.iti.weather.fake.FakeWeatherLocManager
import com.mad.iti.weather.fake.FakeWeatherRepo
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainViewModelTest {


    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MainViewModel
    private lateinit var fakeWeatherLocManager: FakeWeatherLocManager
    private lateinit var fakeWeatherRepo: FakeWeatherRepo

    @Before
    fun setUp() {
        fakeWeatherRepo = FakeWeatherRepo()
        fakeWeatherLocManager = FakeWeatherLocManager(30.0, 30.0,40.0,40.0)
        viewModel = MainViewModel(fakeWeatherRepo, fakeWeatherLocManager)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getLocationFromGPS 30,30  returnLocation30,30`() = runTest {
        // when

        viewModel.requestLocationUpdateByGPS()

        val latLng = viewModel.location.getOrAwaitValue { }



        assertThat(latLng.latitude, CoreMatchers.`is`(30.0))
        assertThat(latLng.longitude, CoreMatchers.`is`(30.0))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getLocationFromMap 40,40  returnLocation40,40`() = runTest {
        // when
//        mainRule.pauseDispatcher()
        viewModel.requestLocationUpdateSavedFromMap()

//        mainRule.resumeDispatcher()
        val latLng = viewModel.location.getOrAwaitValue { }



        assertThat(latLng.latitude, CoreMatchers.`is`(40.0))
        assertThat(latLng.longitude, CoreMatchers.`is`(40.0))

    }


}