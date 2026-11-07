package com.example.shared.data.repositorys


import com.example.shared.data.local.AppSetting
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.domain.lock.AppLockRepository
import com.example.shared.domain.lock.DEFAULT_TIMEOUT_SECONDS
import com.example.shared.domain.lock.LockConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightAppLockRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : AppLockRepository {

    private val q get() = db.appSettingQueries

    override fun load() = LockConfig(
        onboardingDone = read(ONBOARDING) == "1",
        pinHash = read(PIN_HASH),
        pinSalt = read(PIN_SALT),
        pinIterations = read(PIN_ITERATIONS)?.toIntOrNull() ?: 0,
        biometricEnabled = read(BIOMETRIC) == "1",
        timeoutSeconds = read(TIMEOUT)?.toIntOrNull() ?: DEFAULT_TIMEOUT_SECONDS,
        screenshotsAllowed = read(SCREENSHOTS) == "1",
        failedAttempts = read(FAILED)?.toIntOrNull() ?: 0,
        lockoutUntilMillis = read(LOCKOUT_UNTIL)?.toLongOrNull() ?: 0L
    )

    override suspend fun save(config: LockConfig) = withContext(dispatcher) {
        db.transaction {
            write(ONBOARDING, if (config.onboardingDone) "1" else "0")
            write(PIN_HASH, config.pinHash)
            write(PIN_SALT, config.pinSalt)
            write(PIN_ITERATIONS, config.pinIterations.toString())
            write(BIOMETRIC, if (config.biometricEnabled) "1" else "0")
            write(TIMEOUT, config.timeoutSeconds.toString())
            write(SCREENSHOTS, if (config.screenshotsAllowed) "1" else "0")
            write(FAILED, config.failedAttempts.toString())
            write(LOCKOUT_UNTIL, config.lockoutUntilMillis.toString())
        }
    }

    private fun read(key: String): String? = q.selectSetting(key).executeAsOneOrNull()

    private fun write(key: String, value: String?) {
        if (value == null) q.deleteSetting(key) else q.upsertSetting(AppSetting(key, value))
    }

    private companion object {
        const val ONBOARDING = "lock_onboarding_done"
        const val PIN_HASH = "lock_pin_hash"
        const val PIN_SALT = "lock_pin_salt"
        const val PIN_ITERATIONS = "lock_pin_iterations"
        const val BIOMETRIC = "lock_biometric"
        const val TIMEOUT = "lock_timeout_seconds"
        const val SCREENSHOTS = "lock_screenshots_allowed"
        const val FAILED = "lock_failed_attempts"
        const val LOCKOUT_UNTIL = "lock_lockout_until"
    }
}