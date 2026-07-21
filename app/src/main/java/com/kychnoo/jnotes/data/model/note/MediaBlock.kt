package com.kychnoo.jnotes.data.model.note

import java.util.UUID

interface MediaBlock {
    val uri: String
    val filePrefix: String
    val fileExtension: String

    fun copyWithUri(newUri: String): NoteBlock

    fun generateFileName(): String = "${filePrefix}_${UUID.randomUUID()}.$fileExtension"
}