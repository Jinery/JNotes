package com.kychnoo.jnotes.data.repository

import com.kychnoo.jnotes.R
import com.kychnoo.jnotes.core.ext.onAsyncFailure
import com.kychnoo.jnotes.crypto.EncryptedFileManager
import com.kychnoo.jnotes.crypto.NoteCryptoManager
import com.kychnoo.jnotes.data.local.dao.NoteDao
import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import com.kychnoo.jnotes.data.local.entity.NoteEntity
import com.kychnoo.jnotes.data.local.relation.NoteWithBlocks
import com.kychnoo.jnotes.data.model.note.DecryptedNote
import com.kychnoo.jnotes.data.model.note.EncryptedNote
import com.kychnoo.jnotes.data.model.note.MediaBlock
import com.kychnoo.jnotes.data.model.note.NoteBlock
import com.kychnoo.jnotes.data.model.note.toNoteBlock
import com.kychnoo.jnotes.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val cryptoManager: NoteCryptoManager,
    private val fileManager: EncryptedFileManager,
    private val resourceProvider: ResourceProvider
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    fun getAllNotesWithBlocks(): Flow<List<NoteWithBlocks>> = noteDao.getNotesWithBlocks()
    fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun getDecryptedNote(noteId: String, password: String): Result<DecryptedNote?> = runCatching {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@runCatching null
        if (!note.isEncrypted) {
            val blocks = noteDao.getBlocksForNote(noteId)
            DecryptedNote(
                note.title,
                blocks.map { it.toNoteBlock() }
            )
        } else {
            val payload = note.encryptedPayload ?: throw IllegalStateException("No payload found for encrypted note")
            cryptoManager.decryptNote(password, EncryptedNote(note.encryptedTitle, payload))
        }
    }.onAsyncFailure()

    suspend fun saveNote(
        title: String,
        blocks: List<NoteBlock>,
        password: String? = null,
        isPinned: Boolean = false,
        encryptTitle: Boolean = false
    ): Result<Unit> = runCatching {
        if (!password.isNullOrBlank()) {
            val noteTitle = if (encryptTitle) resourceProvider.getString(R.string.encrypted_note_title) else title
            val encryptedNote = cryptoManager.encryptNote(password, blocks, title.takeIf { encryptTitle })
            noteDao.insertNote(NoteEntity.encrypted(
                title = noteTitle,
                encryptedNote = encryptedNote,
                isPinned = isPinned
            ))
        } else {
            val createdNote = NoteEntity.default(title = title, isPinned = isPinned)
            val noteBlocks = blocks.mapIndexed { index, block ->
                NoteBlockEntity.fromNoteBlock(
                    noteBlock = block,
                    noteId = createdNote.id,
                    orderIndex = index
                )
            }
            noteDao.insertFullNote(note = createdNote, blocks = noteBlocks)
        }
    }.onAsyncFailure()

    suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
        noteDao.getBlocksForNote(noteId).forEach { block ->
            if (block.type.isMedia) {
                runCatching {
                    val noteBlock = NoteBlock.fromJSON(block.payloadJson)
                    if (noteBlock is MediaBlock) {
                        fileManager.deleteFile(noteBlock.uri)
                    }
                }.onFailure { fileEx ->
                    fileEx.printStackTrace()
                }
            }
        }
        noteDao.deleteNoteById(noteId)
    }.onAsyncFailure()

    suspend fun togglePin(noteId: String): Result<Unit>
        = runCatching { noteDao.togglePin(noteId) }.onAsyncFailure()
}