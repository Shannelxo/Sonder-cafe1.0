package com.sondercafe.app.core.validation

/**
 * Cleans text typed by the user before it is validated or stored.
 *
 * Room always uses bound query parameters, so this isn't about SQL injection. It stops hidden
 * control characters, accidental whitespace and over-long values from getting into the
 * database and later breaking the layout or search.
 */
object InputSanitizer {

    private val controlChars = Regex("\\p{Cntrl}")
    private val controlCharsExceptNewline = Regex("[\\p{Cntrl}&&[^\\n]]")
    private val whitespaceRun = Regex("\\s+")
    private val horizontalWhitespaceRun = Regex("[ \\t]+")
    private val extraBlankLines = Regex("\\n{3,}")

    /** For names, search queries and other single-line fields. */
    fun singleLine(input: String, maxLength: Int): String =
        input
            .replace(controlChars, " ")
            .replace(whitespaceRun, " ")
            .trim()
            .take(maxLength)
            .trimEnd()

    /** For comments: keeps line breaks but limits blank lines to one in a row. */
    fun multiLine(input: String, maxLength: Int): String =
        input
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace(controlCharsExceptNewline, "")
            .replace(horizontalWhitespaceRun, " ")
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(extraBlankLines, "\n\n")
            .trim()
            .take(maxLength)
            .trimEnd()

    /** Emails are compared case-insensitively, so they're stored lower-case. */
    fun email(input: String): String =
        input.replace(whitespaceRun, "").lowercase()
}
