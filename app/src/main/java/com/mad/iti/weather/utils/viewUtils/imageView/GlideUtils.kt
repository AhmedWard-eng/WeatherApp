package com.mad.iti.weather.viewUtils

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.setImageFromNetworkUrL(urlString: String){
    Glide.with(this)
        .load(urlString)
        .into(this)
}

fun ImageView.setImageFromWeatherIconId(iconId: String){

    val urlString = "https://openweathermap.org/img/wn/$iconId.png"
    Glide.with(this)
        .load(urlString)
        .into(this)
}
fun ImageView.setImageFromWeatherIconId2x(iconId: String){

    val urlString = "https://openweathermap.org/img/wn/$iconId@2x.png"
    Glide.with(this)
        .load(urlString)
        .into(this)
}
fun ImageView.setImageFromWeatherIconId4x(iconId: String){

    val urlString = "https://openweathermap.org/img/wn/$iconId@4x.png"
    Glide.with(this)
        .load(urlString)
        .into(this)
}