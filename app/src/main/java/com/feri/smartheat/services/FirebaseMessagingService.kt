package com.feri.smartheat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit
import com.feri.smartheat.MainActivity
import com.feri.smartheat.R

class FirebaseMessagingService  : FirebaseMessagingService() {

    private fun sendNotification(title: String, message: String) {
        val channelId = "ch-1"
        val notificationId = 1

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "SmartHeat Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "SmartHeat push notifications"
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    override fun onNewToken(token: String) {
        Log.d("FIREBASE NEW TOKEN", token)
        super.onNewToken(token)
        getSharedPreferences("_", MODE_PRIVATE).edit { putString("fb", token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FIREBASE_MESSAGE", message.toString())

        val title = message.notification?.title ?: "SmartHeat"
        val body = message.notification?.body ?: "You have a new message."

        sendNotification(title, body)
    }

    object FirebaseMessagingServiceUtils {
        fun getToken(context: Context): String {
            val string = context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")
            return string ?: "empty"
        }
    }

}