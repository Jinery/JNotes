package com.kychnoo.jnotes.domain.model

import kotlinx.serialization.Serializable

// Types for blocks in Note.
@Serializable
enum class BlockType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO;

        val isMedia: Boolean get() = this != TEXT
}