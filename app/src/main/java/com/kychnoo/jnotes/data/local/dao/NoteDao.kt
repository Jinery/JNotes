package com.kychnoo.jnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import com.kychnoo.jnotes.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: String): NoteEntity?

    @Query("SELECT * FROM note_blocks WHERE noteId = :noteId ORDER BY orderIndex")
    suspend fun getBlocksForNote(noteId: String): List<NoteBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<NoteBlockEntity>)

    @Transaction
    suspend fun insertFullNote(note: NoteEntity, blocks: List<NoteBlockEntity>) {
        insertNote(note)
        insertBlocks(blocks)
    }

    @Query("DELETE FROM note_blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksForNote(noteId: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :now WHERE id = :id")
    suspend fun togglePin(id: String, now: Long = System.currentTimeMillis())
}