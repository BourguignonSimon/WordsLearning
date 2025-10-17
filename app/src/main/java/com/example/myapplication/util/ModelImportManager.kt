package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

class ModelImportManager(private val context: Context) {

    enum class ModelType { VOSK, WHISPER }

    data class ImportResult(
        val success: Boolean,
        val message: String,
        val destination: File?
    )

    fun importModel(uri: Uri, type: ModelType): ImportResult {
        val document = DocumentFile.fromSingleUri(context, uri)
            ?: return ImportResult(false, "URI invalide", null)
        val modelsDir = File(context.filesDir, "models/${type.name.lowercase()}").apply { mkdirs() }
        val destination = File(modelsDir, document.name ?: "model_${System.currentTimeMillis()}")
        if (destination.exists()) {
            return ImportResult(false, "Un modèle avec ce nom existe déjà", null)
        }
        return try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return ImportResult(false, "Impossible de lire le fichier", null)
            ImportResult(true, "Modèle importé", destination)
        } catch (throwable: Throwable) {
            ImportResult(false, throwable.message ?: "Erreur inconnue", null)
        }
    }
}
