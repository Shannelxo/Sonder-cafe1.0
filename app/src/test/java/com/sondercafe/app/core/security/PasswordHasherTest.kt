package com.sondercafe.app.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class PasswordHasherTest {

    // Low iteration count keeps the tests fast. The algorithm is the same.
    private val hasher = PasswordHasher(iterations = 1_000)

    @Test
    fun `verifies the correct password`() {
        val hashed = hasher.hash("coffee123")
        assertTrue(hasher.verify("coffee123", hashed.hash, hashed.salt))
    }

    @Test
    fun `rejects a wrong password`() {
        val hashed = hasher.hash("coffee123")
        assertFalse(hasher.verify("coffee124", hashed.hash, hashed.salt))
        assertFalse(hasher.verify("Coffee123", hashed.hash, hashed.salt))
    }

    @Test
    fun `same password gets a different salt and hash each time`() {
        val first = hasher.hash("coffee123")
        val second = hasher.hash("coffee123")
        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
    }

    @Test
    fun `verification fails if the salt is changed`() {
        val hashed = hasher.hash("coffee123")
        val otherSalt = hasher.hash("something-else").salt
        assertFalse(hasher.verify("coffee123", hashed.hash, otherSalt))
    }

    @Test
    fun `hash is 256 bits and salt is 128 bits`() {
        val hashed = hasher.hash("coffee123")
        assertEquals(32, Base64.getDecoder().decode(hashed.hash).size)
        assertEquals(16, Base64.getDecoder().decode(hashed.salt).size)
    }

    @Test
    fun `stored hash does not contain the password`() {
        val hashed = hasher.hash("coffee123")
        assertFalse(hashed.hash.contains("coffee123"))
    }
}
