package com.kychnoo.jnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kychnoo.jnotes.data.model.note.NoteBlock
import com.kychnoo.jnotes.domain.model.BlockType
import java.util.UUID

@Entity(
    tableName = "note_blocks",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE // Remove all note blocks on delete a note.
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class NoteBlockEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val type: BlockType,
    val orderIndex: Int,
    val payloadJson: String
) {
    companion object {
        fun fromNoteBlock(
            noteBlock: NoteBlock,
            noteId: String,
            orderIndex: Int,
        ): NoteBlockEntity = NoteBlockEntity(
            id = UUID.randomUUID().toString(),
            noteId = noteId,
            type = noteBlock.blockType,
            orderIndex = orderIndex,
            payloadJson = noteBlock.toJson()
        )
    }
}
