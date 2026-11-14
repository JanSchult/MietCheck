package com.example.shared.data.repositorys


import com.example.shared.data.local.AppSetting
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.domain.model.TenantProfile
import com.example.shared.domain.repository.TenantProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightTenantProfileRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TenantProfileRepository {

    private val q get() = db.appSettingQueries

    override suspend fun get(): TenantProfile = withContext(dispatcher) {
        TenantProfile(
            name = read(NAME).orEmpty(),
            email = read(EMAIL).orEmpty(),
            phone = read(PHONE).orEmpty()
        )
    }

    override suspend fun save(profile: TenantProfile) = withContext(dispatcher) {
        db.transaction {
            write(NAME, profile.name.trim())
            write(EMAIL, profile.email.trim())
            write(PHONE, profile.phone.trim())
        }
    }

    private fun read(key: String): String? = q.selectSetting(key).executeAsOneOrNull()

    private fun write(key: String, value: String) {
        if (value.isBlank()) q.deleteSetting(key) else q.upsertSetting(AppSetting(key, value))
    }

    private companion object {
        const val NAME = "tenant_name"
        const val EMAIL = "tenant_email"
        const val PHONE = "tenant_phone"
    }
}