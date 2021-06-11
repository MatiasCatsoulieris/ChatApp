package android.example.com.chatapp.model

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.lang.NullPointerException

class AppConfiguration : Application() {

    companion object {
        val CHANNEL_NEW_MESSAGE = "channelNewMessages"
    }
    override fun onCreate() {
        super.onCreate()
        enableFirebaseCache()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_NEW_MESSAGE,
                    "New Messages", NotificationManager.IMPORTANCE_HIGH)
                channel.enableLights(true)
                channel.lightColor = Color.RED
                channel.description = "Notification for new messages"

                val manager = getSystemService(NotificationManager::class.java)

                manager.createNotificationChannel(channel)
            }
        } catch(e: NullPointerException) {
            e.message
        }
    }

    private fun enableFirebaseCache() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}