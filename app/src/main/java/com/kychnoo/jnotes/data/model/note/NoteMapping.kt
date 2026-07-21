package com.kychnoo.jnotes.data.model.note

import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import kotlinx.serialization.json.Json

fun NoteBlockEntity.toNoteBlock(): NoteBlock = Json.decodeFromString(payloadJson)
