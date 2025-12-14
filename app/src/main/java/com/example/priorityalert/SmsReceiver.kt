package com.example.priorityalert

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.app.NotificationCompat

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val priorityAlertManager = PriorityAlertManager(context)
            if (priorityAlertManager.getEnabled()) {
                val contacts = priorityAlertManager.getContacts()
                val triggerPhrase = priorityAlertManager.getKeyword()

                if (contacts.isNotEmpty() && !triggerPhrase.isNullOrBlank()) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                    for (smsMessage in messages) {
                        val sender = smsMessage.originatingAddress
                        val messageBody = smsMessage.messageBody

                        if (sender != null && contacts.any { sender.contains(it) } && messageBody.contains(triggerPhrase, ignoreCase = true)) {
                            val contactName = getContactName(context, sender) ?: sender

                            val fullScreenIntent = Intent(context, AlertActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                putExtra("sender", contactName)
                            }

                            val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                            val notificationBuilder = NotificationCompat.Builder(context, "priority_alert_channel")
                                .setSmallIcon(R.drawable.ic_launcher_foreground) // You'll need to add an icon here
                                .setContentTitle("Priority Alert")
                                .setContentText("Incoming alert from $contactName")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_CALL)
                                .setFullScreenIntent(fullScreenPendingIntent, true)

                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(1, notificationBuilder.build())

                            break // Trigger alert once per set of messages
                        }
                    }
                }
            }
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
        return null
    }
}
