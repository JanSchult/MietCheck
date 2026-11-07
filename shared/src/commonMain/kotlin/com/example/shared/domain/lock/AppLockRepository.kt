package com.example.shared.domain.lock

interface AppLockRepository {
    fun load(): LockConfig                      // synchron beim Start
    suspend fun save(config: LockConfig)
}