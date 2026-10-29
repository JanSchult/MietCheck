package com.example.shared.domain.platform

sealed interface PlatformFileResult {
    data class Success(val path: String, val bytes: ByteArray) : PlatformFileResult
    data class Error(val message: String) : PlatformFileResult
}