
package com.example.mpaz_pro_1.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH = 256

    fun hash(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = derive(password, salt)

        return "pbkdf2-sha256:$ITERATIONS:" +
                "${Base64.encodeToString(salt, Base64.NO_WRAP)}:" +
                Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verify(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 4 || parts[0] != "pbkdf2-sha256") {
                return false
            }

            val iterations = parts[1].toInt()
            if (iterations !in 100_000..1_000_000) return false

            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[3], Base64.NO_WRAP)
            val actualHash = derive(password, salt, iterations)

            java.security.MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        iterations: Int = ITERATIONS
    ): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            KEY_LENGTH
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}
