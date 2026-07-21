package com.kychnoo.jnotes.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import com.kychnoo.jnotes.data.local.entity.NoteEntity

data class NoteWithBlocks(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val blocks: List<NoteBlockEntity>
) {
    val sortedBlocks: List<NoteBlockEntity> get() = blocks.sortedBy { it.orderIndex }
}
