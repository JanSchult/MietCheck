package com.example.shared.data.local


import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class DatabaseKeyProvider(context: Context) {

    private val prefs = context.getSharedPreferences("mietcheck_secure", Context.MODE_PRIVATE)

    fun getOrCreatePassphrase(): ByteArray = getOrCreateSecret(PREF_DB)
    fun getOrCreateFileKey(): ByteArray = getOrCreateSecret(PREF_FILE)

    private fun getOrCreateSecret(prefKey: String): ByteArray {
        prefs.getString(prefKey, null)?.let { stored ->
            return decrypt(Base64.decode(stored, Base64.NO_WRAP))
        }
        val secret = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encoded = Base64.encodeToString(encrypt(secret), Base64.NO_WRAP)
        check(prefs.edit().putString(prefKey, encoded).commit()) { "Schlüssel konnte nicht gespeichert werden" }
        return secret
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        return cipher.iv + cipher.doFinal(plain)          // 12 Byte IV + Ciphertext
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        val iv = blob.copyOfRange(0, IV_SIZE)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        }
        return cipher.doFinal(blob, IV_SIZE, blob.size - IV_SIZE)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "mietcheck_db_key"
        const val PREF_DB = "db_passphrase"      // gleicher Wert wie bisher, bestehende Installationen laufen weiter
        const val PREF_FILE = "file_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
    }
}