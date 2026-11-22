package com.sondercafe.app.core.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    // Display name

    @Test
    fun `accepts normal names including accents and apostrophes`() {
        assertNull(Validators.displayName("Sam"))
        assertNull(Validators.displayName("Zoë O'Brien"))
        assertNull(Validators.displayName("Anne-Marie"))
    }

    @Test
    fun `rejects blank, too short and too long names`() {
        assertNotNull(Validators.displayName(""))
        assertNotNull(Validators.displayName("A"))
        assertNotNull(Validators.displayName("a".repeat(Validators.NAME_MAX_LENGTH + 1)))
    }

    @Test
    fun `rejects names with digits or symbols`() {
        assertNotNull(Validators.displayName("Sam123"))
        assertNotNull(Validators.displayName("<script>"))
    }

    // Email

    @Test
    fun `accepts valid email addresses`() {
        assertNull(Validators.email("sam@example.com"))
        assertNull(Validators.email("sam.smith+cafe@mail.example.co.uk"))
    }

    @Test
    fun `rejects invalid email addresses`() {
        listOf("", "sam", "sam@", "@example.com", "sam@example", "sam@@example.com", "sam..x@example.com", "sam @example.com")
            .forEach { assertNotNull("Expected '$it' to be rejected", Validators.email(it)) }
    }

    @Test
    fun `rejects email addresses that are too long`() {
        val longEmail = "a".repeat(250) + "@x.com"
        assertNotNull(Validators.email(longEmail))
    }

    // Password

    @Test
    fun `accepts a password with letters and numbers`() {
        assertNull(Validators.password("coffee123"))
    }

    @Test
    fun `rejects short passwords`() {
        assertNotNull(Validators.password("abc123"))
    }

    @Test
    fun `rejects passwords without a letter or without a number`() {
        assertNotNull(Validators.password("12345678"))
        assertNotNull(Validators.password("abcdefgh"))
    }

    @Test
    fun `rejects passwords over the maximum length`() {
        assertNotNull(Validators.password("a1".repeat(Validators.PASSWORD_MAX_LENGTH)))
    }

    @Test
    fun `password confirmation must match exactly`() {
        assertNull(Validators.passwordConfirmation("coffee123", "coffee123"))
        assertNotNull(Validators.passwordConfirmation("coffee123", "Coffee123"))
    }

    // Quantity, rating, comment

    @Test
    fun `quantity boundaries`() {
        assertNotNull(Validators.quantity(0))
        assertNull(Validators.quantity(1))
        assertNull(Validators.quantity(20))
        assertNotNull(Validators.quantity(21))
    }

    @Test
    fun `rating boundaries`() {
        assertNotNull(Validators.rating(0))
        assertNull(Validators.rating(1))
        assertNull(Validators.rating(5))
        assertNotNull(Validators.rating(6))
    }

    @Test
    fun `comment length limit`() {
        assertNull(Validators.comment(""))
        assertNull(Validators.comment("a".repeat(Validators.COMMENT_MAX_LENGTH)))
        assertNotNull(Validators.comment("a".repeat(Validators.COMMENT_MAX_LENGTH + 1)))
    }
}
