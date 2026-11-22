package com.sondercafe.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.security.PasswordHasher
import com.sondercafe.app.core.validation.InputSanitizer
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.data.local.dao.UserDao
import com.sondercafe.app.data.local.entity.UserEntity
import com.sondercafe.app.data.mapper.toDomain
import com.sondercafe.app.data.session.SessionManager
import com.sondercafe.app.domain.model.Role
import com.sondercafe.app.domain.model.User
import com.sondercafe.app.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val passwordHasher: PasswordHasher
) : AuthRepository {

    // Failed-login tracking. Kept in memory, so it resets if the app is killed; enough to slow
    // down guessing on the device without locking a real user out for long.
    private val loginLock = Mutex()
    private var failedAttempts = 0
    private var lockedUntilMillis = 0L

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<User?> = sessionManager.userId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun register(displayName: String, email: String, password: String): AppResult<User> {
        val cleanName = InputSanitizer.singleLine(displayName, Validators.NAME_MAX_LENGTH)
        val cleanEmail = InputSanitizer.email(email)

        // The screen validates too, but the repository doesn't rely on it.
        val validationError = Validators.displayName(cleanName)
            ?: Validators.email(cleanEmail)
            ?: Validators.password(password)
        if (validationError != null) return AppResult.Error(validationError)

        return try {
            if (userDao.findByEmail(cleanEmail) != null) {
                return AppResult.Error(DUPLICATE_EMAIL)
            }
            val hashed = withContext(Dispatchers.Default) { passwordHasher.hash(password) }
            val entity = UserEntity(
                email = cleanEmail,
                displayName = cleanName,
                passwordHash = hashed.hash,
                passwordSalt = hashed.salt,
                role = Role.CUSTOMER,
                createdAt = Instant.now()
            )
            val id = userDao.insert(entity)
            sessionManager.setUserId(id)
            AppResult.Success(entity.copy(id = id).toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteConstraintException) {
            AppResult.Error(DUPLICATE_EMAIL, e)
        } catch (e: Exception) {
            AppResult.Error("Couldn't create your account. Please try again.", e)
        }
    }

    override suspend fun login(email: String, password: String): AppResult<User> = loginLock.withLock {
        val now = System.currentTimeMillis()
        if (now < lockedUntilMillis) {
            val seconds = ((lockedUntilMillis - now) / 1000) + 1
            return@withLock AppResult.Error("Too many failed attempts. Try again in $seconds seconds.")
        }

        try {
            val user = userDao.findByEmail(InputSanitizer.email(email))
            val passwordMatches = withContext(Dispatchers.Default) {
                if (user == null) {
                    // Do the same amount of work for an unknown email, so timing doesn't reveal
                    // which emails have accounts.
                    passwordHasher.hash(password)
                    false
                } else {
                    passwordHasher.verify(password, user.passwordHash, user.passwordSalt)
                }
            }

            if (user == null || !passwordMatches) {
                failedAttempts++
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    failedAttempts = 0
                    lockedUntilMillis = now + LOCKOUT_MILLIS
                    return@withLock AppResult.Error("Too many failed attempts. Try again in ${LOCKOUT_MILLIS / 1000} seconds.")
                }
                // Same message whether the email or the password was wrong.
                return@withLock AppResult.Error("Email or password is incorrect")
            }

            failedAttempts = 0
            sessionManager.setUserId(user.id)
            AppResult.Success(user.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error("Couldn't log you in. Please try again.", e)
        }
    }

    override suspend fun logout() {
        sessionManager.clear()
    }

    override suspend fun deleteAccount(): AppResult<Unit> = try {
        val id = sessionManager.userId.first()
        if (id == null) {
            AppResult.Error("You're not logged in")
        } else {
            // Foreign keys cascade the delete to the cart, orders and feedback.
            userDao.deleteById(id)
            sessionManager.clear()
            AppResult.Success(Unit)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error("Couldn't delete your account. Please try again.", e)
    }

    private companion object {
        const val DUPLICATE_EMAIL = "An account with this email already exists"
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_MILLIS = 30_000L
    }
}
