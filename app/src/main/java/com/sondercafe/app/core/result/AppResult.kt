package com.sondercafe.app.core.result

/**
 * Result of an operation that can fail in a way the user should know about.
 *
 * Repositories catch low-level exceptions (database, network) and return [Error] with a
 * message that is safe to show on screen. The original exception is kept in [Error.cause]
 * for logging and tests, but never shown to the user.
 */
sealed interface AppResult<out T> {

    data class Success<out T>(val data: T) : AppResult<T>

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (AppResult.Error) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(this)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data
