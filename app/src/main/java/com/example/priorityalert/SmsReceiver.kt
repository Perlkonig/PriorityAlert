package com.example.priorityalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony

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
                            val alertIntent = Intent(context, AlertActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra("sender", contactName)
                            }
                            context.startActivity(alertIntent)
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
