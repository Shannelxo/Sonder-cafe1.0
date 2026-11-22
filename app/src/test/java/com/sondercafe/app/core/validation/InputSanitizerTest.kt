package com.sondercafe.app.core.validation

import org.junit.Assert.assertEquals
import org.junit.Test

class InputSanitizerTest {

    @Test
    fun `singleLine trims and collapses whitespace`() {
        assertEquals("Flat white", InputSanitizer.singleLine("   Flat    white  ", 50))
    }

    @Test
    fun `singleLine turns control characters and newlines into spaces`() {
        assertEquals("Oat latte", InputSanitizer.singleLine("Oat\u0000latte", 50))
        assertEquals("Oat latte", InputSanitizer.singleLine("Oat\nlatte", 50))
        assertEquals("Oat latte", InputSanitizer.singleLine("Oat\tlatte", 50))
    }

    @Test
    fun `singleLine cuts long input to the maximum length`() {
        assertEquals("abcde", InputSanitizer.singleLine("abcdefgh", 5))
    }

    @Test
    fun `singleLine does not leave a trailing space after cutting`() {
        assertEquals("abc", InputSanitizer.singleLine("abc defgh", 4))
    }

    @Test
    fun `singleLine of blank input is empty`() {
        assertEquals("", InputSanitizer.singleLine("  \n\t ", 50))
    }

    @Test
    fun `multiLine keeps single line breaks`() {
        assertEquals("Great coffee.\nFriendly staff.", InputSanitizer.multiLine("Great coffee.\nFriendly staff.", 500))
    }

    @Test
    fun `multiLine normalises windows line endings`() {
        assertEquals("one\ntwo", InputSanitizer.multiLine("one\r\ntwo", 500))
    }

    @Test
    fun `multiLine limits blank lines to one in a row`() {
        assertEquals("one\n\ntwo", InputSanitizer.multiLine("one\n\n\n\n\ntwo", 500))
    }

    @Test
    fun `multiLine removes control characters and trims each line`() {
        assertEquals("nice\ncake", InputSanitizer.multiLine("  nice\u0007  \n   cake  ", 500))
    }

    @Test
    fun `email is lower-cased with spaces removed`() {
        assertEquals("sam@example.com", InputSanitizer.email("  Sam@Example.COM "))
    }
}
