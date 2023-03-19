package com.mad.iti.weather.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.model.OneCallRepo
import com.mad.iti.weather.model.OneCallRepoInterface
import com.mad.iti.weather.networkUtils.APIStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"

class HomeViewModel(private val _repo: OneCallRepoInterface) : ViewModel() {

    val weather: StateFlow<APIStatus>
        get() = _repo.weatherFlow


    class Factory(private val _repo: OneCallRepoInterface) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(_repo) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}