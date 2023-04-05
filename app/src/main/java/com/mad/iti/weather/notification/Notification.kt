package com.mad.iti.weather.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat.*
import com.mad.iti.weather.BuildConfig
import com.mad.iti.weather.R

//var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//    .setSmallIcon(R.drawable.baseline_add_alarm_24)
//    .setContentTitle(textTitle)
//    .setContentText(textContent)
//    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, message:String) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        channel.setShowBadge(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

//    val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)

//    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
//    val stackBuilder = TaskStackBuilder.create(context)
//        .addParentStack(ReminderDescriptionActivity::class.java)
//        .addNextIntent(intent)
//    val notificationPendingIntent = stackBuilder
//        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

//    build the notification object with the data to be shown
    val notification = Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
        .setContentTitle("weather notification")
        .setContentText(message)
        .setCategory(CATEGORY_ALARM)
        .setPriority(PRIORITY_MAX)
        .setDefaults(Notification.DEFAULT_ALL)
        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
//        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(0, notification)
}
