package com.mad.iti.weather.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mad.iti.weather.MapsActivity
import com.mad.iti.weather.R
import com.mad.iti.weather.databinding.FragmentSettingBinding
import com.mad.iti.weather.language.changeLanguageLocaleTo
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences.Companion.NAVIGATE_TO_MAP
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences.Companion.SET_LOCATION_AS_MAIN_LOCATION

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding


    private val settingSharedPreferences by lazy {
        SettingSharedPreferences.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getLanguageLocale() == "ar") {
            binding.radioButtonArabic.toggle()
        } else {
            binding.radioButtonEnglish.toggle()
        }
        when (settingSharedPreferences.getWindSpeedPref()) {
            SettingSharedPreferences.METER_PER_SECOND -> binding.radioButtonMPerSec.toggle()
            SettingSharedPreferences.MILE_PER_HOUR -> binding.radioButtonMilePerHour.toggle()
        }

        when (settingSharedPreferences.getLocationPref()) {
            SettingSharedPreferences.GPS -> binding.radioButtonGPS.toggle()
            SettingSharedPreferences.MAP -> binding.radioButtonMap.toggle()
        }

        when (settingSharedPreferences.getTempPref()) {
            SettingSharedPreferences.CELSIUS -> binding.radioButtonC.toggle()
            SettingSharedPreferences.KELVIN -> binding.radioButtonK.toggle()
            SettingSharedPreferences.FAHRENHEIT -> binding.radioButtonF.toggle()
        }

        binding.radioGroupChooseLanguage.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_Arabic -> changeLanguageLocaleTo("ar")
                R.id.radio_button_English -> changeLanguageLocaleTo("en")
            }
        }
        binding.radioGroupLocation.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_GPS -> {
                    settingSharedPreferences.setLocationPref(
                        SettingSharedPreferences.GPS
                    )
                    requireActivity().recreate()
                }
                R.id.radio_button_map -> {
                    settingSharedPreferences.setLocationPref(
                        SettingSharedPreferences.MAP
                    )
                    with(Intent(requireContext(), MapsActivity::class.java)) {
                        putExtra(NAVIGATE_TO_MAP, SET_LOCATION_AS_MAIN_LOCATION)
                        startActivity(this)
                    }
                }
            }
        }
        binding.radioGroupWindSpeed.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_MPerSec -> settingSharedPreferences.setWindSpeedPref(
                    SettingSharedPreferences.METER_PER_SECOND
                )
                R.id.radio_button_MilePerHour -> settingSharedPreferences.setWindSpeedPref(
                    SettingSharedPreferences.MILE_PER_HOUR
                )
            }
        }
        binding.radioGroupTempDegree.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_C -> settingSharedPreferences.setTempPref(
                    SettingSharedPreferences.CELSIUS
                )
                R.id.radio_button_K -> settingSharedPreferences.setTempPref(
                    SettingSharedPreferences.KELVIN
                )
                R.id.radio_button_F -> settingSharedPreferences.setTempPref(
                    SettingSharedPreferences.FAHRENHEIT
                )
            }
        }


    }


}