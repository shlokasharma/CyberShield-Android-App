package com.example.advance_smtg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SMS_DEBUG", "!!! BROADCAST RECEIVED !!!")

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val detector = ScamDetector()
            val db = DatabaseHelper(context)

            for (sms in messages) {
                val body = sms.messageBody ?: ""
                val result = detector.analyzeWithXai(body)

                if (result.isScam) {
                    val reason = result.reasons.joinToString(", ")
                    db.logThreat("SMS", body, reason)

                    // 1. Show the Notification
                    showNotification(context, "Scam Detected!", "Reason: $reason")

                    // 2. Open the Red Alert Screen
                    val alertIntent = Intent(context, HighRiskActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(alertIntent)
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "scam_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel (Required for Android 8.0+)
        val channel = NotificationChannel(channelId, "Security Alerts", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Built-in icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}