package com.mad.iti.weather.model

import com.mad.iti.weather.networkUtils.APIStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface OneCallRepoInterface {
   suspend fun enqueueWeatherCall(lat: String, lon: String)
    val weatherFlow: StateFlow<APIStatus>
}
