package com.sondercafe.app.core.validation

/**
 * Validation rules for user input. Each function returns an error message to show next to the
 * field, or null if the value is fine.
 *
 * Text should be passed through [InputSanitizer] first, so these checks see the value that
 * will actually be stored.
 */
object Validators {

    const val NAME_MIN_LENGTH = 2
    const val NAME_MAX_LENGTH = 50
    const val EMAIL_MAX_LENGTH = 254
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 64
    const val COMMENT_MAX_LENGTH = 500
    const val SEARCH_MAX_LENGTH = 50
    const val MIN_QUANTITY = 1
    const val MAX_QUANTITY = 20
    const val MIN_RATING = 1
    const val MAX_RATING = 5

    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val namePattern = Regex("^[\\p{L} .'-]+$")

    fun displayName(name: String): String? = when {
        name.isBlank() -> "Enter your name"
        name.length < NAME_MIN_LENGTH -> "Name must be at least $NAME_MIN_LENGTH characters"
        name.length > NAME_MAX_LENGTH -> "Name can be up to $NAME_MAX_LENGTH characters"
        !namePattern.matches(name) -> "Name can only contain letters, spaces, hyphens and apostrophes"
        else -> null
    }

    fun email(email: String): String? = when {
        email.isBlank() -> "Enter your email address"
        email.length > EMAIL_MAX_LENGTH -> "Email address is too long"
        email.contains("..") || !emailPattern.matches(email) -> "Enter a valid email address"
        else -> null
    }

    fun password(password: String): String? = when {
        password.length < PASSWORD_MIN_LENGTH -> "Password must be at least $PASSWORD_MIN_LENGTH characters"
        password.length > PASSWORD_MAX_LENGTH -> "Password can be up to $PASSWORD_MAX_LENGTH characters"
        password.none { it.isLetter() } || password.none { it.isDigit() } ->
            "Password must include at least one letter and one number"
        else -> null
    }

    fun passwordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Passwords don't match" else null

    fun quantity(quantity: Int): String? =
        if (quantity !in MIN_QUANTITY..MAX_QUANTITY) {
            "Quantity must be between $MIN_QUANTITY and $MAX_QUANTITY"
        } else {
            null
        }

    fun rating(rating: Int): String? =
        if (rating !in MIN_RATING..MAX_RATING) "Choose a rating from $MIN_RATING to $MAX_RATING stars" else null

    fun comment(comment: String): String? =
        if (comment.length > COMMENT_MAX_LENGTH) "Comments can be up to $COMMENT_MAX_LENGTH characters" else null
}
