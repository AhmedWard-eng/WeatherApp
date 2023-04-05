package com.mad.iti.weather.ui.alert

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mad.iti.weather.MapsActivity
import com.mad.iti.weather.R
import com.mad.iti.weather.databinding.DialogTitleBinding
import com.mad.iti.weather.databinding.FragmentAlertBinding
import com.mad.iti.weather.databinding.SetAlarmDialogueBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.model.FavAlertsWeatherRepo
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.AlertKind
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
import com.mad.iti.weather.utils.statusUtils.AlertsAPIStatus
import com.mad.iti.weather.utils.viewUtils.textView.setDate
import com.mad.iti.weather.utils.viewUtils.textView.setTime
import com.mad.iti.weather.worker.AlertWorker
import com.mad.iti.weather.worker.ID
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class AlertFragment : Fragment() {

    private var _binding: FragmentAlertBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var customAlertDialogBinding: SetAlarmDialogueBinding
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder


    private val alertViewModel by lazy {
        val factory = AlertViewModel.Factory(
            FavAlertsWeatherRepo.getInstance(
                APIClient, DefaultLocalDataSource.getInstance(
                    getDatabase(requireContext()).weatherDao
                )
            )
        )
        ViewModelProvider(this, factory)[AlertViewModel::class.java]
    }


    private fun sendToEnableIt() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + requireContext().packageName)
        )
        someActivityResultLauncher.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertBinding.inflate(inflater, container, false)
        askAboutPermissions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AlertsAdapter(AlertsAdapter.RemoveClickListener {

            checkDeleteDialog(it)
        })

        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alertViewModel.alerts.collect { status ->
                    when (status) {
                        is AlertsAPIStatus.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is AlertsAPIStatus.Success -> {
                            adapter.submitList(status.alertEntityList)
                            if (status.alertEntityList.isNotEmpty()) {
                                binding.progressBar.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                binding.groupNoAlarms.visibility = View.GONE
                            } else {
                                binding.progressBar.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.groupNoAlarms.visibility = View.VISIBLE
                            }
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.groupNoAlarms.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }


        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        floatingActionButton = binding.floatingActionButton

        floatingActionButton.setOnClickListener {
            customAlertDialogBinding =
                SetAlarmDialogueBinding.inflate(LayoutInflater.from(requireContext()), null, false)
            // Launching the custom alert dialog
            launchCustomAlertDialog()
        }

    }


    private fun launchCustomAlertDialog() {
        val title = DialogTitleBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        title.textTitle.text = getString(R.string.set_alarm)
        val alertDialog = materialAlertDialogBuilder.setView(customAlertDialogBinding.root)
            .setCustomTitle(title.root).setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialogue_background, requireActivity().theme
                )
            ).setCancelable(false).show()
        setTimeAndDateInDialog()


        var startTime = Calendar.getInstance().timeInMillis
        val endCal = Calendar.getInstance()
        endCal.add(Calendar.DAY_OF_MONTH, 1)
        var endTime = endCal.timeInMillis

        customAlertDialogBinding.buttonSave.setOnClickListener {
            val id = if (customAlertDialogBinding.radioAlarm.isChecked) {
                saveToDatabase(startTime, endTime, AlertKind.ALARM)
            } else {
                saveToDatabase(startTime, endTime, AlertKind.NOTIFICATION)
            }

            scheduleWork(startTime, endTime, id)
            checkDisplayOverOtherAppPerm()
            with(Intent(requireContext(), MapsActivity::class.java)) {
                putExtra(
                    SettingSharedPreferences.NAVIGATE_TO_MAP,
                    SettingSharedPreferences.ADD_T0_ALERTS_IN_THIS_LOCATION
                )
                putExtra(ID, id)
                startActivity(this)
            }
            alertDialog.dismiss()
        }
        customAlertDialogBinding.cardViewChooseStart.setOnClickListener {
            setAlarm(startTime) { currentTime ->
                startTime = currentTime
                customAlertDialogBinding.textViewStartDate.setDate(currentTime)
                customAlertDialogBinding.textViewStartTime.setTime(currentTime)
            }
        }
        customAlertDialogBinding.cardViewChooseEnd.setOnClickListener {
            setAlarm(endTime) { currentTime ->
                endTime = currentTime
                customAlertDialogBinding.textViewEndDate.setDate(currentTime)
                customAlertDialogBinding.textViewEndTime.setTime(currentTime)
            }
        }
        customAlertDialogBinding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun saveToDatabase(startTime: Long, endTime: Long, alarmKind: String): String {
        val alertEntity =
            AlertEntity(start = startTime, end = endTime, kind = alarmKind, lat = 0.0, lon = 0.0)
        alertViewModel.insertIntoAlerts(alertEntity)
        return alertEntity.id
    }

    private fun scheduleWork(startTime: Long, endTime: Long, tag: String) {

        val _Day_TIME_IN_MILLISECOND = 24 * 60 * 60 * 1000L
        val timeNow = Calendar.getInstance().timeInMillis

        val inputData = Data.Builder()
        inputData.putString(ID, tag)


        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val myWorkRequest: WorkRequest = if ((endTime - startTime) < _Day_TIME_IN_MILLISECOND) {
            Log.d("TAG", "scheduleWork: one")
            OneTimeWorkRequestBuilder<AlertWorker>().addTag(tag).setInitialDelay(
                startTime - timeNow, TimeUnit.MILLISECONDS
            ).setInputData(
                inputData = inputData.build()
            ).setConstraints(constraints).build()

        } else {

            WorkManager.getInstance(requireContext()).enqueue(
                OneTimeWorkRequestBuilder<AlertWorker>().addTag(tag).setInitialDelay(
                    startTime - timeNow, TimeUnit.MILLISECONDS
                ).setInputData(
                    inputData = inputData.build()
                ).setConstraints(constraints).build()
            )

            Log.d("TAG", "scheduleWork: periodic")

            PeriodicWorkRequest.Builder(
                AlertWorker::class.java, 24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS
            ).addTag(tag).setInputData(
                inputData = inputData.build()
            ).setConstraints(constraints).build()
        }
        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
    }

    private fun checkDisplayOverOtherAppPerm() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireContext().packageName)
            )
            someActivityResultLauncher.launch(intent)
        }
    }


    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (!Settings.canDrawOverlays(requireContext())) {
//                Snackbar.make(
//                    binding.root,
//                    getString(R.string.The_alarm_may_be_not_work_as_expected),
//                    LENGTH_LONG
//                ).setAction("Enable") {
//                    sendToEnableIt()
//                }.show()
            }
        }


    private fun setTimeAndDateInDialog() {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        customAlertDialogBinding.textViewStartDate.setDate(currentTime)
        customAlertDialogBinding.textViewStartTime.setTime(currentTime)
        val timeAfterOneHour = calendar.get(Calendar.HOUR_OF_DAY)
        calendar.set(Calendar.HOUR_OF_DAY, timeAfterOneHour + 2)
        customAlertDialogBinding.textViewEndDate.setDate(calendar.timeInMillis)
        customAlertDialogBinding.textViewEndTime.setTime(calendar.timeInMillis)
    }

    private fun setAlarm(minTime: Long, callback: (Long) -> Unit) {
        val color = ResourcesCompat.getColor(resources, R.color.textColor, requireActivity().theme)
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            val datePickerDialog = DatePickerDialog(
                requireContext(), R.style.DialogTheme, { _, year, month, day ->
                    this.set(Calendar.YEAR, year)
                    this.set(Calendar.MONTH, month)
                    this.set(Calendar.DAY_OF_MONTH, day)
                    val timePickerDialog = TimePickerDialog(
                        requireContext(), R.style.DialogTheme, { _, hour, minute ->
                            this.set(Calendar.HOUR_OF_DAY, hour)
                            this.set(Calendar.MINUTE, minute)
                            callback(this.timeInMillis)
                        }, this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE), false
                    )
                    timePickerDialog.show()
                    timePickerDialog.setCancelable(false)
                    timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
                    timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
                },

                this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH)

            )
            datePickerDialog.datePicker.minDate = minTime
            datePickerDialog.show()
            datePickerDialog.setCancelable(false)
            datePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
            datePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)

        }
    }

//    private fun runBackgroundPermissions() {
//        if (Build.BRAND.equals("xiaomi", ignoreCase = true)) {
//                // autostart is enabled for sure
//                val intent = Intent()
//                intent.component = ComponentName(
//                    "com.miui.securitycenter",
//                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
//                )
//                startActivity(intent)
//
//
//        } else if (Build.BRAND.equals(
//                "Honor", ignoreCase = true
//            ) || Build.BRAND.equals("HUAWEI", ignoreCase = true)
//        ) {
//            val intent = Intent()
//            intent.component = ComponentName(
//                "com.huawei.systemmanager",
//                "com.huawei.systemmanager.optimize.process.ProtectActivity"
//            )
//            startActivity(intent)
//        }
//
////        val intent = Intent()
////        val manufacturer = Build.MANUFACTURER
////        if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
////            intent.component = ComponentName(
////                "com.miui.securitycenter",
////                "com.miui.permcenter.autostart.AutoStartManagementActivity"
////            )
////            val pm: PackageManager = requireActivity().packageManager
////            val resolveInfoList =
////                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
////            if (resolveInfoList.size > 0) {
////                // Auto-start management activity is available
////                // Check if the app has auto-start permission enabled
////                val packageName: String = requireContext().packageName
////                val intent1 = Intent()
////                intent1.component = ComponentName(
////                    "com.miui.securitycenter",
////                    "com.miui.permcenter.autostart.AutoStartDetailActivity"
////                )
////                intent1.putExtra("package_name", packageName)
////                val resolveInfoList1 =
////                    pm.queryIntentActivities(intent1, PackageManager.MATCH_DEFAULT_ONLY)
////                if (resolveInfoList1.size > 0) {
////                    // Auto-start detail activity is available
////                    // Check if the app has auto-start permission enabled
////                    var autoStartEnabled = false
////                    try {
////                        val applicationInfo = pm.getApplicationInfo(packageName, 0)
////                        val bundle = applicationInfo.metaData
////                        if (bundle != null) {
////                            autoStartEnabled = bundle.getBoolean("miui_autostart")
////                        }
////                    } catch (e: PackageManager.NameNotFoundException) {
////                        // handle exception
////                    }
////                    if (autoStartEnabled) {
////                        // Auto-start permission is enabled for the app
////                    } else {
////                        val i = Intent()
////                        i.component = ComponentName(
////                            "com.miui.securitycenter",
////                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
////                        )
////                        startActivity(i)
////                    }
////                }
////            }
////        }
//
//
//    }

    private fun askAboutPermissions() {
        // Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            //when screen is black but not locked it will light-up
            requireActivity().setShowWhenLocked(true)
            requireActivity().setTurnScreenOn(true)
        }
        if (!Settings.canDrawOverlays(requireActivity())) {
            checkDrawOverAppsPermissionsDialog()
        }

//        runBackgroundPermissions()
    }

    private fun checkDrawOverAppsPermissionsDialog() {
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.permission_request))
            .setCancelable(false)
            .setMessage(getString(R.string.please_allow_draw_over_apps_permission))
            .setPositiveButton(
                "Yes"
            ) { _, _ -> checkDisplayOverOtherAppPerm() }.setNegativeButton(
                "No"
            ) { _, _ -> errorWarningForNotGivingDrawOverAppsPermissions() }.show()
    }

    private fun checkDeleteDialog(alertEntity: AlertEntity) {
        val deleteAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        deleteAlertDialogBuilder.setBackground(
            ResourcesCompat.getDrawable(
                resources, R.drawable.dialogue_background, requireActivity().theme
            )
        ).setTitle(getString(R.string.assure_deleting)).setCancelable(false).setPositiveButton(
            getString(R.string.yes)
        ) { _, _ ->
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(alertEntity.id)
            alertViewModel.removeFromAlerts(alertEntity)
        }.setNegativeButton(
            getString(R.string.no)
        ) { _, _ -> }.show()
    }


    private fun errorWarningForNotGivingDrawOverAppsPermissions() {
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.warning))
            .setCancelable(false).setMessage(
                getString(R.string.unfortunately_the_display_over_other_apps_permission_is_not_granted)
            ).setPositiveButton(android.R.string.ok) { _, _ -> }.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}