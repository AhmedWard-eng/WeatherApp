package com.mad.iti.weather.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun changeLanguageLocaleTo(lan: String) {
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lan)
    // Call this on the main thread as it may require Activity.restart()
    AppCompatDelegate.setApplicationLocales(appLocale)
//    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}

fun getLanguageLocale(): String {
    return AppCompatDelegate.getApplicationLocales().toLanguageTags()
}