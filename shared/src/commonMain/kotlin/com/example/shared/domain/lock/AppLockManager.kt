package com.example.shared.domain.lock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

const val PIN_LENGTH = 6
const val DEFAULT_TIMEOUT_SECONDS = 60
val AUTO_LOCK_OPTIONS_SECONDS = listOf(0, 60, 300, 900)

private const val FREE_ATTEMPTS = 5
private const val HASH_ITERATIONS = 100_000
private const val EXTERNAL_FLOW_GRACE_MS = 10 * 60 * 1000L
private val LOCKOUT_SCHEDULE_SECONDS = listOf(30L, 60L, 300L, 900L, 3600L)

class AppLockManager(
    private val repository: AppLockRepository,
    private val hasher: PinHasher
) {
    private val mutex = Mutex()

    private val _config = MutableStateFlow(repository.load())
    val config: StateFlow<LockConfig> = _config.asStateFlow()

    private val _state = MutableStateFlow(initialState(_config.value))
    val state: StateFlow<LockState> = _state.asStateFlow()

    private var backgroundedAt: Long? = null
    private var externalFlowDeadline: Long = 0L

    // ---------- Lebenszyklus (wird vom Android-Application-Objekt aufgerufen) ----------

    fun onAppBackgrounded() {
        backgroundedAt = nowMillis()
    }

    fun onAppForegrounded() {
        if (_state.value != LockState.UNLOCKED || !_config.value.hasPin) return
        val now = nowMillis()
        if (now <= externalFlowDeadline) {          // z. B. Scanner, Dateiauswahl, Teilen-Menü
            externalFlowDeadline = 0L
            return
        }
        val since = backgroundedAt ?: return
        val elapsed = now - since
        // Negativ = Uhr wurde zurückgestellt: sicherheitshalber sperren
        if (elapsed < 0 || elapsed >= _config.value.timeoutSeconds * 1000L) lock()
    }

    /** Vor dem Start einer Fremd-Activity aufrufen, damit der Rückweg nicht sperrt (max. 10 Minuten). */
    fun beginExternalFlow() {
        externalFlowDeadline = nowMillis() + EXTERNAL_FLOW_GRACE_MS
    }

    fun lock() {
        if (_config.value.hasPin) _state.value = LockState.LOCKED
    }

    fun unlock() {
        _state.value = LockState.UNLOCKED
    }

    // ---------- PIN ----------

    suspend fun checkPin(pin: String): PinCheck = mutex.withLock {
        val cfg = _config.value
        val hash = cfg.pinHash
        val salt = cfg.pinSalt
        if (hash == null || salt == null) return@withLock PinCheck.Correct

        val now = nowMillis()
        if (now < cfg.lockoutUntilMillis) return@withLock PinCheck.LockedOut(secondsUntil(cfg.lockoutUntilMillis - now))

        val attempts = cfg.failedAttempts + 1
        persist(cfg.copy(failedAttempts = attempts))           // vor dem Prüfen speichern

        val candidate = hasher.hash(pin, salt.hexToBytes(), cfg.pinIterations)
        if (constantTimeEquals(candidate, hash.hexToBytes())) {
            persist(_config.value.copy(failedAttempts = 0, lockoutUntilMillis = 0L))
            return@withLock PinCheck.Correct
        }

        val delaySeconds = lockoutDelaySeconds(attempts)
        if (delaySeconds > 0) {
            persist(_config.value.copy(lockoutUntilMillis = now + delaySeconds * 1000))
            PinCheck.LockedOut(delaySeconds)
        } else {
            PinCheck.Wrong(FREE_ATTEMPTS - attempts)
        }
    }

    suspend fun unlockWithPin(pin: String): PinCheck {
        val result = checkPin(pin)
        if (result is PinCheck.Correct) unlock()
        return result
    }

    suspend fun setPin(pin: String) {
        require(isValidPin(pin)) { "Die PIN muss aus $PIN_LENGTH Ziffern bestehen." }
        mutex.withLock {
            val salt = hasher.randomSalt()
            val hash = hasher.hash(pin, salt, HASH_ITERATIONS)
            persist(
                _config.value.copy(
                    pinHash = hash.toHex(), pinSalt = salt.toHex(), pinIterations = HASH_ITERATIONS,
                    failedAttempts = 0, lockoutUntilMillis = 0L
                )
            )
        }
    }

    suspend fun removePin() = update {
        it.copy(pinHash = null, pinSalt = null, pinIterations = 0, biometricEnabled = false,
            failedAttempts = 0, lockoutUntilMillis = 0L)
    }

    fun lockoutRemainingSeconds(): Long {
        val remaining = _config.value.lockoutUntilMillis - nowMillis()
        return if (remaining <= 0) 0 else secondsUntil(remaining)
    }

    /** Sekundenticker für die Sperrzeit-Anzeige, endet bei 0. */
    fun lockoutTicker(): Flow<Long> = flow {
        while (true) {
            val seconds = lockoutRemainingSeconds()
            emit(seconds)
            if (seconds <= 0) break
            delay(1000)
        }
    }

    // ---------- Einstellungen ----------

    suspend fun finishOnboarding() {
        update { it.copy(onboardingDone = true) }
        _state.value = LockState.UNLOCKED
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = update { it.copy(biometricEnabled = enabled && it.hasPin) }
    suspend fun setTimeoutSeconds(seconds: Int) = update { it.copy(timeoutSeconds = seconds) }
    suspend fun setScreenshotsAllowed(allowed: Boolean) = update { it.copy(screenshotsAllowed = allowed) }

    // ---------- intern ----------

    private suspend fun update(block: (LockConfig) -> LockConfig) = mutex.withLock { persist(block(_config.value)) }

    private suspend fun persist(new: LockConfig) {
        repository.save(new)
        _config.value = new
    }

    private fun initialState(cfg: LockConfig) = when {
        !cfg.onboardingDone -> LockState.NEEDS_ONBOARDING
        cfg.hasPin -> LockState.LOCKED          // kalter Start: immer gesperrt
        else -> LockState.UNLOCKED
    }

    private fun lockoutDelaySeconds(attempts: Int): Long =
        if (attempts < FREE_ATTEMPTS) 0L
        else LOCKOUT_SCHEDULE_SECONDS[(attempts - FREE_ATTEMPTS).coerceAtMost(LOCKOUT_SCHEDULE_SECONDS.lastIndex)]

    private fun secondsUntil(millis: Long): Long = (millis + 999) / 1000
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

private fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
private fun String.hexToBytes(): ByteArray = ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
    return diff == 0
}