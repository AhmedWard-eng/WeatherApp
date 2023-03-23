package com.mad.iti.weather

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.mad.iti.weather.location.WeatherLocationManager
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import com.mad.iti.weather.model.OneCallRepoInterface
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"
class MainViewModel(private val _repo: OneCallRepoInterface,private val _locManager : WeatherLocationManager) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location :LiveData<Location> get() = _location
    fun getWeather(lat: String, lon: String) {
        viewModelScope.launch {
            _repo.enqueueWeatherCall(lat, lon)
        }
    }
    init {
        viewModelScope.launch {

            Log.d(TAG, "onCreateView000: ${_locManager.location.javaClass}")
            _locManager.location.collect{
                if(it is LocationStatus.Success){
                    _location.postValue(it.location)
                    _locManager.removeLocationUpdate()
                }
            }
        }
    }

    fun requestLocationUpdate(){
       _locManager.requestLocation()
   }
    fun isLocationEnabled(): Boolean{
        return _locManager.isLocationEnabled()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val _repo: OneCallRepoInterface, private val _loc: WeatherLocationManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(_repo,_loc) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}