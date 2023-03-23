package com.mad.iti.weather.utils.viewUtils.textView

import android.widget.TextView
import com.mad.iti.weather.utils.getDayFormat
import com.mad.iti.weather.utils.getTimeFormat

fun TextView.setTime(timeInSecond: Int) {
    text = getTimeFormat(timeInSecond * 1000L)
}

fun TextView.setTime(timeInMilliSecond: Long) {
    text = getTimeFormat(timeInMilliSecond)
}

fun TextView.setDay(timeInSecond: Int) {
    setText(getDayFormat(timeInSecond * 1000L))
}


