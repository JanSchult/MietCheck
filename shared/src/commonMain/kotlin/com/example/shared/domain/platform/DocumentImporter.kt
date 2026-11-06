package com.example.shared.domain.platform

interface DocumentImporter {
    val canPick: Boolean
    val canScan: Boolean
    fun pickFile()
    fun scan()
}