package com.mad.iti.weather.ui.home


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mad.iti.weather.fake.FakeWeatherLocManager
import com.mad.iti.weather.fake.FakeWeatherRepo
import com.mad.iti.weather.MainDispatcherRule
import com.mad.iti.weather.utils.statusUtils.APIStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class HomeViewModelTest {
    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeWeatherLocManager: FakeWeatherLocManager
    private lateinit var fakeWeatherRepo: FakeWeatherRepo

    @Before
    fun setUp() {
        fakeWeatherRepo = FakeWeatherRepo()
        fakeWeatherLocManager = FakeWeatherLocManager(30.0, 30.0, 40.0, 40.0)
        viewModel = HomeViewModel(fakeWeatherRepo, fakeWeatherLocManager)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun `enqueueWork   returnSucceeded`() = runTest {
        assertThat(fakeWeatherRepo.weatherFlow.value is APIStatus.Loading, CoreMatchers.`is`(true))
        viewModel.getWeather(20.0, 20.0)

        launch {
            fakeWeatherRepo.weatherFlow.collect{
                assertThat(it is APIStatus.Success, CoreMatchers.`is`(true))
                cancel()
            }
        }


    }
}