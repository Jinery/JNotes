package com.kychnoo.jnotes.crypto

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest


class EncryptedFileManager(
    @ApplicationContext private val context: Context
) {
    private val mediaDir: File get() = File(context.filesDir, "media").apply { mkdirs() }
    private val thumbsDir: File get() = File(context.filesDir, "thumbs").apply { mkdirs() }

    suspend fun saveFile(sourceUri: Uri, fileName: String, password: String? = null): String
        = withContext(Dispatchers.IO) {
            val ext = fileName.substringAfterLast('.', "bin")
            val hash = sourceUri.hashUri()
            val baseName = "${ext}_${hash}"
            val destFile = if (password.isNullOrBlank()) { File(mediaDir, "$baseName.ext") }
                else { File(mediaDir, "$baseName.enc") }
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val dataToWrite = if (!password.isNullOrBlank()) NativeCrypto.encryptData(password, bytes) else bytes
                FileOutputStream(destFile).use { it.write(dataToWrite) }
            } ?: throw IllegalStateException("Cannot open input stream for $sourceUri")
            destFile.absolutePath
        }

    suspend fun readFile(path: String, password: String? = null): ByteArray
        = withContext(Dispatchers.IO) {
            val file = File(path)
            val data = file.readBytes()
            if (!password.isNullOrBlank()) NativeCrypto.decryptData(password, data) else data
        }

    suspend fun deleteFile(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.delete()) {
            val hash = file.nameWithoutExtension.substringAfterLast(delimiter = '_')
            File(thumbsDir, "thumb_$hash.jpg").delete()
        }
    }

    fun isEncrypted(path: String): Boolean = path.endsWith(suffix = ".enc")

    private fun Uri.hashUri(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(this.toString().toByteArray()).joinToString(separator = "") { "%02x".format(it) }.take(16)
    }
}