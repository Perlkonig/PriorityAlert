package com.example.priorityalert

import android.content.Context
import android.net.Uri

class PriorityAlertManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("PriorityAlertPrefs", Context.MODE_PRIVATE)

    fun saveContacts(contacts: Set<String>) {
        sharedPreferences.edit().putStringSet("contacts", contacts).apply()
    }

    fun getContacts(): Set<String> {
        return sharedPreferences.getStringSet("contacts", emptySet()) ?: emptySet()
    }

    fun saveRingtone(uri: Uri) {
        sharedPreferences.edit().putString("ringtone", uri.toString()).apply()
    }

    fun getRingtone(): Uri? {
        val uriString = sharedPreferences.getString("ringtone", null)
        return uriString?.let { Uri.parse(it) }
    }

    fun saveKeyword(keyword: String) {
        sharedPreferences.edit().putString("keyword", keyword).apply()
    }

    fun getKeyword(): String? {
        return sharedPreferences.getString("keyword", null)
    }

    fun saveEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("enabled", enabled).apply()
    }

    fun getEnabled(): Boolean {
        return sharedPreferences.getBoolean("enabled", true)
    }
}