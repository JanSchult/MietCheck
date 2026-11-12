package com.example.shared.domain.model

data class StoredMedia(val storedName: String, val kind: MediaKind, val sizeBytes: Long)

const val MAX_PHOTO_BYTES = 25L * 1024 * 1024
const val MAX_VIDEO_BYTES = 200L * 1024 * 1024

fun mediaKindOf(storedName: String): MediaKind =
    if (storedName.startsWith("v-")) MediaKind.VIDEO else MediaKind.PHOTO
