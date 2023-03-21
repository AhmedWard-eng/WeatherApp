package com.mad.iti.weather

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mad.iti.weather.location.LocationManager
import com.mad.iti.weather.model.OneCallRepoInterface
import kotlinx.coroutines.launch

class MainViewModel(private val _repo: OneCallRepoInterface,private val _locManager : LocationManager) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location :LiveData<Location> get() = _location
    fun getWeather(lat: String, lon: String) {
        viewModelScope.launch {
            _repo.enqueueWeatherCall(lat, lon)
        }
    }
   fun requestLocationUpdate(){
       _locManager.requestLocation { loc ->
           _location.postValue(loc)
           _locManager.removeLocationUpdate()
       }
   }
    fun isLocationEnabled(): Boolean{
        return _locManager.isLocationEnabled()
    }

    class Factory(private val _repo: OneCallRepoInterface, private val _loc: LocationManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(_repo,_loc) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}