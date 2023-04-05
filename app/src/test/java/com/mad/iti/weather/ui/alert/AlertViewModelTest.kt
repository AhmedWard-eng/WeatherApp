package com.mad.iti.weather.ui.alert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mad.iti.weather.MainDispatcherRule
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.fake.FakeFavAlertsWeatherRepo
import com.mad.iti.weather.utils.statusUtils.AlertsAPIStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AlertViewModelTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    private lateinit var viewModel: AlertViewModel
    private lateinit var fakeFavAlertsWeatherRepo: FakeFavAlertsWeatherRepo

    @Before
    fun setUp() {
        fakeFavAlertsWeatherRepo = FakeFavAlertsWeatherRepo()
        viewModel = AlertViewModel(fakeFavAlertsWeatherRepo)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun whenInserting_returnSize1() = runTest {
        val a = AlertEntity(start = 5L, end = 5L, kind = "", lon = 2.0, lat = 2.0)
        viewModel.insertIntoAlerts(a)

        launch {
            viewModel.alerts.collect {

                it as AlertsAPIStatus.Success

                assertThat(it.alertEntityList.size, `is`(1))
                cancel()
            }
        }

    }

    @ExperimentalCoroutinesApi
    @Test
    fun whenRemovedAlertAfterInserting_returnSize0() = runTest {
        val a = AlertEntity(start = 5L, end = 5L, kind = "", lon = 2.0, lat = 2.0)
        viewModel.insertIntoAlerts(a)

        viewModel.removeFromAlerts(a)
        launch {
            viewModel.alerts.collect {

                it as AlertsAPIStatus.Success

                assertThat(it.alertEntityList.size, `is`(0))
                cancel()
            }
        }
    }


}