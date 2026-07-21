package com.kychnoo.jnotes.crypto

import android.content.Context
import android.util.Base64
import com.kychnoo.jnotes.data.model.note.EncryptedNote
import com.kychnoo.jnotes.data.model.note.NoteBlock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.kychnoo.jnotes.data.model.note.DecryptedNote
import com.kychnoo.jnotes.data.model.note.MediaBlock
import kotlinx.serialization.json.Json
import org.json.JSONArray

class NoteCryptoManager(
    private val encryptedFileManager: EncryptedFileManager
) {
    suspend fun encryptNote(
        password: String,
        blocks: List<NoteBlock>,
        title: String? = null,
    ): EncryptedNote = withContext(Dispatchers.IO) {
        val encryptedBlocks = blocks.map { it.encryptBlock(password) }
        val serializedBlocks = encryptedBlocks.serializeBlocks()
        val encryptedPayload = NativeCrypto.encryptData(password, serializedBlocks.toByteArray(Charsets.UTF_8))
        val encryptedTitle = title
            ?.takeIf { it.isNotBlank() }
            ?.let { NativeCrypto.encryptString(password, it) }
        EncryptedNote(
            encryptedTitle = encryptedTitle,
            encryptedPayload = Base64.encodeToString(encryptedPayload, Base64.NO_WRAP)
        )
    }

    suspend fun decryptNote(
        password: String,
        encryptedNote: EncryptedNote
    ): DecryptedNote = withContext(Dispatchers.IO) {
        val title = encryptedNote.encryptedTitle
            ?.takeIf { it.isNotBlank() }
            ?.let { NativeCrypto.decryptString(password, it) }
        val payloadBytes = NativeCrypto.decryptData(password, Base64.decode(encryptedNote.encryptedPayload, Base64.NO_WRAP))
        val blocks = String(payloadBytes, Charsets.UTF_8).deserializeBlocks()
        DecryptedNote(title, blocks)
    }

    private suspend fun NoteBlock.encryptBlock(password: String): NoteBlock {
        return if (this !is MediaBlock) {
            this
        } else {
            val name = this.generateFileName()
            val encryptedUri = encryptedFileManager.saveFile(this.uri.toUri(), name, password)
            this.copyWithUri(encryptedUri)
        }
    }

    private fun List<NoteBlock>.serializeBlocks(): String {
        return Json.encodeToString(this)
    }

    private fun String.deserializeBlocks(): List<NoteBlock> {
        return Json.decodeFromString(this)
    }
}