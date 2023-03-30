package com.mad.iti.weather.checkNetowrk

import android.app.Application
import android.content.Context
import android.net.*
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "NetworkConnectivity"

class NetworkConnectivity private constructor(val application: Application) {

    private val networkRequest =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

    private val _connectivitySharedFlow = MutableSharedFlow<NetworkStatus>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val connectivitySharedFlow = _connectivitySharedFlow.asSharedFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "onAvailable: ")
            _connectivitySharedFlow.tryEmit(NetworkStatus.CONNECTED)
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            Log.d(TAG, "onCapabilitiesChanged: $unmetered")
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "onLost: ")
            _connectivitySharedFlow.tryEmit(NetworkStatus.LOST)
        }
    }

    init {
        val connectivityManager =
            application.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
    fun isOnline(): Boolean {
        val connMgr = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
    companion object {
        private lateinit var instance: NetworkConnectivity
        fun getInstance(application: Application): NetworkConnectivity {
            if (!::instance.isInitialized) {
                instance = NetworkConnectivity(application)
            }
            return instance
        }
    }

}

enum class NetworkStatus {
    CONNECTED, LOST
}