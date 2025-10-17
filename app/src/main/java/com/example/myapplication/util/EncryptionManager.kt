package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

class EncryptionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val applicationContext = context.applicationContext

    fun encryptFile(input: File, destination: File) {
        destination.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            destination,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        input.inputStream().use { inputStream ->
            encryptedFile.openFileOutput().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun decryptToTempFile(encrypted: File, tempFile: File): File {
        tempFile.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            encrypted,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encryptedFile.openFileInput().use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

    fun openEncryptedFile(uri: Uri): ByteArray {
        val file = File(uri.path ?: throw IllegalArgumentException("Invalid uri"))
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        return encryptedFile.openFileInput().use { it.readBytes() }
    }
}
