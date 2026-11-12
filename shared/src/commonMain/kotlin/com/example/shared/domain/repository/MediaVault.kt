package com.example.shared.domain.repository

interface MediaVault {
    suspend fun open(storedName: String, share: Boolean)
    suspend fun delete(storedName: String)
}