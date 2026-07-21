package com.kychnoo.jnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kychnoo.jnotes.data.model.note.EncryptedNote
import com.kychnoo.jnotes.data.model.note.NoteBlock
import java.util.UUID

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptedPayload: String? = null,
    val encryptedTitle: String? = null
) {
    companion object {
        fun encrypted(
            title: String,
            encryptedTitle: String?,
            encryptedPayload: String?,
            isPinned: Boolean = false
        ): NoteEntity {
            val now = System.currentTimeMillis()
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = now,
                updatedAt = now,
                isPinned = isPinned,
                isEncrypted = true,
                encryptedPayload = encryptedPayload,
                encryptedTitle = encryptedTitle
            )
        }

        fun encrypted(
            title: String,
            encryptedNote: EncryptedNote,
            isPinned: Boolean = false
        ): NoteEntity = encrypted(
            title = title,
            encryptedTitle = encryptedNote.encryptedTitle,
            encryptedPayload = encryptedNote.encryptedPayload,
            isPinned = isPinned
        )

        fun default(
            title: String,
            isPinned: Boolean = false
        ): NoteEntity {
            val now = System.currentTimeMillis()
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = now,
                updatedAt = now,
                isPinned = isPinned,
                isEncrypted = false
            )
        }
    }
}
