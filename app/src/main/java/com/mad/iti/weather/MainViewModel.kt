package com.mad.iti.weather

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.mad.iti.weather.location.WeatherLocationManagerInterface
import com.mad.iti.weather.utils.locationUtils.LocationStatus
import com.mad.iti.weather.model.WeatherDataRepoInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val _repo: WeatherDataRepoInterface, private val _locManager : WeatherLocationManagerInterface) : ViewModel() {

    private val _location = MutableLiveData<LatLng>()
    val location :LiveData<LatLng> get() = _location

    fun getWeather(lat: String, lon: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.refreshWeatherCall(lat, lon)
        }
    }
    init {
        viewModelScope.launch {
            _locManager.location.collect{
                if(it is LocationStatus.Success){
                    _location.postValue(it.latLng)
                    _locManager.removeLocationUpdate()
                }
            }
        }
    }

    fun requestLocationUpdateByGPS(){
       _locManager.requestLocationByGPS()
   }

    fun requestLocationUpdateSavedFromMap(){
        _locManager.requestLocationSavedFromMap()
    }
    fun isLocationEnabled(): Boolean{
        return _locManager.isLocationEnabled()
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val _repo: WeatherDataRepoInterface, private val _loc: WeatherLocationManagerInterface) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(_repo,_loc) as T
            } else {
                throw java.lang.IllegalArgumentException("ViewModel Class not found")
            }
        }
    }
}