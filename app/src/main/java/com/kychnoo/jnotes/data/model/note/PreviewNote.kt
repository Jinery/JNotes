package com.kychnoo.jnotes.data.model.note

data class PreviewNote(
    val id: String,
    val title: String,
    val isEncrypted: Boolean,
    val previewBlock: NoteBlock? = null,
    val updatedAt: Long
)
