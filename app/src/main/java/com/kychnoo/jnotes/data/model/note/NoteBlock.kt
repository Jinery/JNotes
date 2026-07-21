package com.kychnoo.jnotes.data.model.note

import com.kychnoo.jnotes.domain.model.BlockType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class NoteBlock {
    val blockType: BlockType get() = when (this) {
        is Text -> BlockType.TEXT
        is Image -> BlockType.IMAGE
        is Audio -> BlockType.AUDIO
        is Video -> BlockType.VIDEO
    }

    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJSON(json: String) : NoteBlock {
            return Json.decodeFromString(json)
        }
    }

    @Serializable
    @SerialName("TEXT")
    data class Text(
        val content: String,
        val style: TextStyle
    ) : NoteBlock()

    @Serializable
    @SerialName("IMAGE")
    data class Image(
        override val uri: String,
        val caption: String?
    ) : NoteBlock(), MediaBlock {
        override val filePrefix = "img"
        override val fileExtension = "jpg"

        override fun copyWithUri(newUri: String) = this.copy(uri = newUri)
    }

    @Serializable
    @SerialName("AUDIO")
    data class Audio(
        override val uri: String,
        val durationMs: Long
    ) : NoteBlock(), MediaBlock {
        override val filePrefix = "audio"
        override val fileExtension = "mp3"

        override fun copyWithUri(newUri: String) = this.copy(uri = newUri)
    }

    @Serializable
    @SerialName("VIDEO")
    data class Video(
        override val uri: String
    ) : NoteBlock(), MediaBlock {
        override val filePrefix = "video"
        override val fileExtension = "mp4"

        override fun copyWithUri(newUri: String) = this.copy(uri = newUri)
    }

    @Serializable
    enum class TextStyle { NORMAL, HEADING, BOLD, ITALIC, CODE }
}