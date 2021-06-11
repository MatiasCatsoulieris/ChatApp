package android.example.com.chatapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.example.com.chatapp.R
import android.example.com.chatapp.model.AppConfiguration
import android.example.com.chatapp.model.MessageType
import android.example.com.chatapp.util.CHAT_FRAGMENT
import android.example.com.chatapp.util.CHAT_FRAGMENT_CODE
import android.example.com.chatapp.util.UID
import android.example.com.chatapp.view.MainActivity
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.BundleCompat
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.NullPointerException
import java.util.*

class MessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            showNewMessageNotification(remoteMessage.data)
        }
    }

    private fun showNewMessageNotification(data: MutableMap<String, String>) {
        try {
            val typeNotification = data["typeNotification"]
            val userName = data["userName"]
            val userId = data["userId"]
            val message = data["message"]

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationBuilder = NotificationCompat.Builder(
                applicationContext,
                AppConfiguration.CHANNEL_NEW_MESSAGE
            )

            notificationBuilder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_call_white)
                .setColor(Color.RED)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(userName)

            if (typeNotification != null) {

                when (Integer.parseInt(typeNotification)) {
                    MessageType.TYPE_TEXT.type -> {
                        notificationBuilder.setContentText(message)
                    }
                    MessageType.TYPE_PHOTO.type -> {
                        notificationBuilder.setContentText("Has sent you a photo")
                    }
                    MessageType.TYPE_AUDIO.type -> {
                        notificationBuilder.setContentText("Has sent you an audio")
                    }
                    MessageType.TYPE_VIDEO.type -> {
                        notificationBuilder.setContentText("Has sent you a video")
                    }
                    MessageType.TYPE_DOC_PDF.type -> {
                        notificationBuilder.setContentText("Has sent you a PDF document")
                    }
                    MessageType.TYPE_GIF.type -> {
                        notificationBuilder.setContentText("Has sent you a GIF")
                    }

                }
            }
            if(userId != null) {

                val args = Bundle()
                args.putString(UID, userId)

                val newPendingIntent = NavDeepLinkBuilder(applicationContext)
                    .setDestination(R.id.chatFragment)
                    .setArguments(args)
                    .createPendingIntent()

                notificationBuilder.setContentIntent(newPendingIntent)
            }
            manager.notify(Random().nextInt(), notificationBuilder.build())
        } catch (e: NullPointerException) {
            e.cause
        }
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}