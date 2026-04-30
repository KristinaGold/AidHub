package com.example.aidhub.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.aidhub.R
import com.example.aidhub.activities.MainActivity
import com.example.data.dataStractures.NotificationType
import com.example.aidhub.utilities.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data[Constants.NOTIFICATION_TYPE_KEY]
        val relatedId = remoteMessage.data[Constants.RELATED_ID_KEY]
        val helperId = remoteMessage.data[Constants.HELPER_ID_KEY]
        val notificationId = remoteMessage.data[Constants.NOTIFICATION_ID_KEY]
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["body"]
        var senderId: String? = ""
        if(type == NotificationType.CHAT.displayName)
            senderId = remoteMessage.data[Constants.SENDER_ID_KEY]


        showNotification(title, message, relatedId, type, senderId, helperId, notificationId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCM", "Token updated in DB") }
        }
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(
        title: String?,
        message: String?,
        relatedId: String?,
        type: String?, senderId: String?, helperId: String?, notificationId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java)

        val bundle = Bundle()
        bundle.putString(Constants.RELATED_ID_KEY, relatedId)
        bundle.putString(Constants.NOTIFICATION_TYPE_KEY, type)
        bundle.putString(Constants.SENDER_ID_KEY, senderId)
        bundle.putString(Constants.HELPER_ID_KEY, helperId)
        bundle.putString(Constants.NOTIFICATION_ID_KEY, notificationId)
        intent.putExtra(Constants.BUNDLE_KEY, bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = Constants.NOTIFICATION_CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}