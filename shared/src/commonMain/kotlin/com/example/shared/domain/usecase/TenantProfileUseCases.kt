package com.example.shared.domain.usecase


import com.example.shared.domain.model.TenantProfile
import com.example.shared.domain.repository.TenantProfileRepository

class GetTenantProfileUseCase(private val repository: TenantProfileRepository) {
    suspend operator fun invoke(): TenantProfile = repository.get()
}

class SaveTenantProfileUseCase(private val repository: TenantProfileRepository) {
    suspend operator fun invoke(profile: TenantProfile) = repository.save(profile)
}