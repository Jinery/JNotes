package com.kychnoo.jnotes.data.repository

import com.kychnoo.jnotes.R
import com.kychnoo.jnotes.crypto.EncryptedFileManager
import com.kychnoo.jnotes.crypto.NoteCryptoManager
import com.kychnoo.jnotes.data.local.dao.NoteDao
import com.kychnoo.jnotes.data.local.entity.NoteEntity
import com.kychnoo.jnotes.data.model.note.EncryptedNote
import com.kychnoo.jnotes.data.model.note.NoteBlock
import com.kychnoo.jnotes.provider.ResourceProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.SQLException

class NoteRepositoryTest {

    private val noteDao = mockk<NoteDao>(relaxed = true)
    private val cryptoManager = mockk<NoteCryptoManager>()
    private val fileManager = mockk<EncryptedFileManager>()
    private val resourceProvider = mockk<ResourceProvider>()

    private lateinit var repository: NoteRepository

    @Before
    fun setup() {
        repository = NoteRepository(noteDao, cryptoManager, fileManager, resourceProvider)
    }

    @Test
    fun `saveNote with encryption should call cryptoManager and insertNote`() = runTest {
        // Arrange
        val title = "Secure Note"
        val password = "password123"
        val blocks = listOf(NoteBlock.Text("Content", NoteBlock.TextStyle.NORMAL))
        val encryptedNote = EncryptedNote("encryptedTitle", "encryptedPayload")

        coEvery { cryptoManager.encryptNote(password, blocks, any()) } returns encryptedNote
        every { resourceProvider.getString(R.string.encrypted_note_title) } returns "Encrypted Note"

        // Act
        val result = repository.saveNote(title, blocks, password, encryptTitle = true)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { cryptoManager.encryptNote(password, blocks, title) }
        coVerify { noteDao.insertNote(match { it.isEncrypted && it.title == "Encrypted Note" }) }
    }

    @Test
    fun `saveNote without encryption should call insertFullNote and preserve orderIndex`() = runTest {
        // Arrange
        val title = "Regular Note"
        val blocks = listOf(
            NoteBlock.Text("Block 0", NoteBlock.TextStyle.NORMAL),
            NoteBlock.Text("Block 1", NoteBlock.TextStyle.NORMAL)
        )

        // Act
        val result = repository.saveNote(title, blocks, password = null)

        // Assert
        assertTrue(result.isSuccess)
        coVerify {
            noteDao.insertFullNote(
                note = match { !it.isEncrypted && it.title == title },
                blocks = match { 
                    it.size == 2 && 
                    it[0].orderIndex == 0 &&
                    it[1].orderIndex == 1
                }
            )
        }
    }

    @Test(expected = CancellationException::class)
    fun `saveNote should rethrow CancellationException`() = runTest {
        // Arrange
        coEvery { noteDao.insertFullNote(any(), any()) } throws CancellationException("Job cancelled")

        // Act
        repository.saveNote("Title", emptyList())
    }

    @Test
    fun `saveNote should catch SQLException and return failure`() = runTest {
        // Arrange
        coEvery { noteDao.insertFullNote(any(), any()) } throws SQLException("DB error")

        // Act
        val result = repository.saveNote("Title", emptyList())

        // Assert
        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }

    @Test(expected = CancellationException::class)
    fun `getDecryptedNote should rethrow CancellationException`() = runTest {
        // Arrange
        coEvery { noteDao.getNoteByIdSync(any()) } throws CancellationException()

        // Act
        repository.getDecryptedNote("id", "pass")
    }

    @Test
    fun `getDecryptedNote should catch IllegalStateException and return failure`() = runTest {
        // Arrange
        val encryptedNote = NoteEntity.encrypted("Title", null, null) // No payload
        coEvery { noteDao.getNoteByIdSync("id") } returns encryptedNote

        // Act
        val result = repository.getDecryptedNote("id", "password")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
