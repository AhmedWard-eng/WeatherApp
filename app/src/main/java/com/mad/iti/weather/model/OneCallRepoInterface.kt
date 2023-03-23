package com.mad.iti.weather.model

import com.mad.iti.weather.utils.networkUtils.APIStatus
import kotlinx.coroutines.flow.StateFlow

interface OneCallRepoInterface {
   suspend fun enqueueWeatherCall(lat: String, lon: String)
    val weatherFlow: StateFlow<APIStatus>
}
