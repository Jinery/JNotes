package com.kychnoo.jnotes.domain.model

import kotlinx.serialization.Serializable

// Types for blocks in Note.
@Serializable
enum class BlockType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    LINK_PREVIEW
}