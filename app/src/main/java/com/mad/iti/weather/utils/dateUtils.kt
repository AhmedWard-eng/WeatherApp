package com.mad.iti.weather.utils

import com.mad.iti.weather.R
import java.text.SimpleDateFormat
import java.util.*




//SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
fun getTimeFormat(timeInMilliSecond: Long): String {
    val date = Date(timeInMilliSecond)
    val convertFormat =
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    return convertFormat.format(date).toString()
}

fun getDayFormat(timeInMilliSecond: Long): Int{
    val date = Date(timeInMilliSecond)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return when(calendar.get(Calendar.DAY_OF_WEEK)){
        1-> R.string.sunday
        2-> R.string.monday
        3-> R.string.tuesday
        4-> R.string.wednesday
        5-> R.string.thursday
        6-> R.string.friday
        else -> R.string.saturday
    }
}