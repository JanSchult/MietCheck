package com.example.shared.domain.export

/** Schreibt Einträge nacheinander in ein Archiv auf der Platte und öffnet am Ende das Teilen-Menü. */
interface ExportArchiveWriter {
    suspend fun begin(fileName: String)
    suspend fun addEntry(path: String, bytes: ByteArray)
    suspend fun finishAndShare()
    suspend fun abort()
}