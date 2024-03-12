package gr.georkouk.taskoven.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import gr.georkouk.taskoven.MainActivity
import gr.georkouk.taskoven.R
import gr.georkouk.taskoven.activities.SignInActivity
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.utils.Constants


class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.i(TAG, "Message data payload: " + remoteMessage.data)

            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotification(title, message)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(title: String, message: String) {
        val intent: Intent = if (FirestoreClass().getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        }
        else {
            Intent(this, SignInActivity::class.java)
        }

        // Before lauching the screen add some flags to avoid duplication of activities.
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            0 ,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel MyTrello title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 , notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

}