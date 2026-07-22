package com.kychnoo.jnotes.data.model.note

import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import com.kychnoo.jnotes.data.local.relation.NoteWithBlocks
import kotlinx.serialization.json.Json

fun NoteBlockEntity.toNoteBlock(): NoteBlock = Json.decodeFromString(payloadJson)

fun NoteWithBlocks.toPreviewNote(): PreviewNote {
    val note = this.note
    return PreviewNote(
        id = note.id,
        title = note.title,
        isEncrypted = note.isEncrypted,
        previewBlock = if (note.isEncrypted) null else this.sortedBlocks.firstOrNull()?.toNoteBlock(),
        updatedAt = note.updatedAt
    )
}
