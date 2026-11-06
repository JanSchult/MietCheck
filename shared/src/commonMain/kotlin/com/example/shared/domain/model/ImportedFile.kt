package com.example.shared.domain.model


class ImportedFile(
    val bytes: ByteArray,
    val mimeType: String,
    val suggestedName: String,
    val pageCount: Int,
    val sha256: String
)

const val MAX_IMPORT_BYTES = 25L * 1024 * 1024

/** Erkennt den Typ am Dateiinhalt (Magic Bytes), nicht an Endung oder Name. */
fun detectMimeType(bytes: ByteArray): String? = when {
    bytes.size >= 5 &&
            bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() &&
            bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
            bytes[4] == '-'.code.toByte() -> "application/pdf"
    bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
    bytes.size >= 4 &&
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() -> "image/png"
    else -> null
}

fun extensionFor(mime: String): String = when (mime) {
    "application/pdf" -> "pdf"
    "image/jpeg" -> "jpg"
    "image/png" -> "png"
    else -> "bin"
}