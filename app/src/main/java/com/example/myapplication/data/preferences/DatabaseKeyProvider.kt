package com.example.myapplication.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class DatabaseKeyProvider(private val context: Context) {

    companion object {
        private const val PREF_NAME = "secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }

    fun getOrCreatePassphrase(): CharArray {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val existing = sharedPreferences.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return existing.toCharArray()
        }
        val newKey = generateKey()
        sharedPreferences.edit().putString(KEY_DB_PASSPHRASE, newKey).apply()
        return newKey.toCharArray()
    }

    private fun generateKey(): String {
        val allowed = (('A'..'Z') + ('a'..'z') + ('0'..'9')).toCharArray()
        return buildString(64) {
            repeat(64) {
                append(allowed.random())
            }
        }
    }
}
