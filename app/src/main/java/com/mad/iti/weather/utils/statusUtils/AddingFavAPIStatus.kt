package com.mad.iti.weather.utils.statusUtils


sealed class AddingFavAPIStatus {
    object Success : AddingFavAPIStatus()

    class Failure(var throwable: String) : AddingFavAPIStatus()

    object Loading : AddingFavAPIStatus()
}