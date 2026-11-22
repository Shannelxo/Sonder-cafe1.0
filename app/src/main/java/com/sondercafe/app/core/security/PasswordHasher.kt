package com.sondercafe.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

data class HashedPassword(val hash: String, val salt: String)

/**
 * Hashes passwords with PBKDF2-HMAC-SHA256 and a random salt per user.
 *
 * PBKDF2 is deliberately slow, which makes brute-forcing a copied database expensive. The
 * salt means two users with the same password get different hashes. Hashing takes a noticeable
 * moment, so callers should run it off the main thread.
 */
class PasswordHasher(private val iterations: Int) {

    @Inject
    constructor() : this(DEFAULT_ITERATIONS)

    private val random = SecureRandom()

    fun hash(password: String): HashedPassword {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(password, salt)
        return HashedPassword(hash = encode(hash), salt = encode(salt))
    }

    fun verify(password: String, expectedHash: String, salt: String): Boolean {
        val actual = derive(password, decode(salt))
        // Constant-time comparison, so response time doesn't leak how much of the hash matched.
        return MessageDigest.isEqual(actual, decode(expectedHash))
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)

    companion object {
        const val DEFAULT_ITERATIONS = 210_000
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SALT_BYTES = 16
        private const val KEY_LENGTH_BITS = 256
    }
}
