package com.feri.smartheat.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit

class FirebaseMessagingService  : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FIREBASE NEW TOKEN", token)
        super.onNewToken(token)
        getSharedPreferences("_", MODE_PRIVATE).edit { putString("fb", token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }

    object FirebaseMessagingServiceUtils {
        fun getToken(context: Context): String {
            val string = context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")
            return string ?: "empty"
        }
    }

}