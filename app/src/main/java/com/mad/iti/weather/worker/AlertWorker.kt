package com.mad.iti.weather.worker

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.TextView
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mad.iti.weather.R
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.model.FavAlertsWeatherRepo
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.model.entities.AlertKind
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.notification.sendNotification
import com.mad.iti.weather.utils.locationUtils.formatAddressToCity
import com.mad.iti.weather.utils.locationUtils.getAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

const val ID = "ID"

class AlertWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repo = FavAlertsWeatherRepo.getInstance(
            APIClient,
            DefaultLocalDataSource.getInstance(getDatabase(applicationContext).weatherDao)
        )
        val appContext = applicationContext

        val id = inputData.getString(ID)

        Log.d("TAG", "doWork1: $id")

        return withContext(Dispatchers.IO) {
            if (id != null) {
                try {
                    val alertEntity = repo.getAlertWithId(id)
                    val response = repo.getWeather("${alertEntity.lat}", "${alertEntity.lon}")
                    if (response.isSuccessful) {
                        val alerts = response.body()?.alerts
                        if (alerts != null) {
                            val alertsEvent: String = buildString {
                                for (a in alerts) {
                                    append(a.event)
                                    append("\n")
                                }
                            }
                            when (alertEntity.kind) {
                                AlertKind.ALARM -> createAlarm(appContext, alertsEvent)
                                AlertKind.NOTIFICATION -> sendNotification(appContext, alertsEvent)
                            }
                        } else {
                            getAddress(
                                appContext, alertEntity.lon, alertEntity.lat, Locale(
                                    getLanguageLocale()
                                )
                            ) {
                                when (alertEntity.kind) {

                                    AlertKind.ALARM -> runBlocking {
                                        createAlarm(
                                            appContext, appContext.getString(
                                                R.string.weather_is_fine_alert,
                                                formatAddressToCity(it)
                                            )
                                        )

                                    }
                                    AlertKind.NOTIFICATION -> sendNotification(
                                        appContext,
                                        appContext.getString(
                                            R.string.weather_is_fine_alert,
                                            formatAddressToCity(it)
                                        )
                                    )
                                }

                            }

                        }
                        removeFromDataBaseAndDismiss(repo, alertEntity,appContext)
                        Result.success()
                    } else {
                        Result.retry()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("TAG", "doWork: $e")
                    Result.failure()
                }
            } else {
                Result.failure()
            }
        }


    }

    private suspend fun removeFromDataBaseAndDismiss(
        repo: FavAlertsWeatherRepo,
        alertEntity: AlertEntity,
        appContext: Context
    ) {

        val _Day_TIME_IN_MILLISECOND = 24*60*60*1000L
        val now = Calendar.getInstance().timeInMillis
        if((alertEntity.end -  now)  < _Day_TIME_IN_MILLISECOND){
            WorkManager.getInstance(appContext).cancelAllWorkByTag(alertEntity.id)
            repo.removeFromAlerts(alertEntity)
        }

    }


}


val LAYOUT_FLAG =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) LayoutParams.TYPE_APPLICATION_OVERLAY
    else LayoutParams.TYPE_PHONE

private suspend fun createAlarm(context: Context, message: String) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.nice_alarm)


    val view: View = LayoutInflater.from(context).inflate(R.layout.activity_dialog, null, false)
    val dismissBtn = view.findViewById<Button>(R.id.button_dismiss)
    val textView = view.findViewById<TextView>(R.id.textViewMessage)
    val layoutParams = LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT,
        LAYOUT_FLAG,
        LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )
    layoutParams.gravity = Gravity.CENTER


    val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

    withContext(Dispatchers.Main) {
        windowManager.addView(view, layoutParams)
        view.visibility = VISIBLE
        textView.text = message
    }

    mediaPlayer.start()
    mediaPlayer.isLooping = true
    dismissBtn.setOnClickListener {
        mediaPlayer?.release()
        windowManager.removeView(view)
    }
}