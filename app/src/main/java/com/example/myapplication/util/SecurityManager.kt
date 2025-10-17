package com.example.myapplication.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.MessageDigest

class SecurityManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "security_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
    }

    private val executor = ContextCompat.getMainExecutor(context)

    private val sharedPreferences by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isPinConfigured(): Boolean = sharedPreferences.contains(KEY_PIN_HASH)

    fun updatePin(pin: String) {
        sharedPreferences.edit().putString(KEY_PIN_HASH, sha256(pin)).apply()
    }

    fun validatePin(pin: String): Boolean {
        val hash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        return hash == sha256(pin)
    }

    fun canUseBiometrics(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun buildBiometricPrompt(
        activity: androidx.fragment.app.FragmentActivity,
        onResult: (Boolean) -> Unit
    ): BiometricPrompt {
        val promptInfo = PromptInfo.Builder()
            .setTitle("Déverrouiller l'application")
            .setSubtitle("Authentifiez-vous pour continuer")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Annuler")
            .build()
        return BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(false)
            }

            override fun onAuthenticationFailed() {
                onResult(false)
            }
        }).apply {
            authenticate(promptInfo)
        }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(value.toByteArray())
        return hashed.joinToString(separator = "") { byte ->
            String.format("%02x", byte)
        }
    }
}
