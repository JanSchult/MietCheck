package com.example.shared.domain.repository

import com.example.shared.domain.model.TenantProfile

interface TenantProfileRepository {
    suspend fun get(): TenantProfile
    suspend fun save(profile: TenantProfile)
}