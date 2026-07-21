package com.kychnoo.jnotes.data.model.note

data class DecryptedNote(
    val title: String? = null,
    val blocks: List<NoteBlock>
)
