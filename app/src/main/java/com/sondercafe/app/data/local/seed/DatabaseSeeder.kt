package com.sondercafe.app.data.local.seed

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.sondercafe.app.BuildConfig
import com.sondercafe.app.core.security.PasswordHasher
import com.sondercafe.app.core.validation.InputSanitizer
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.data.local.dao.MenuDao
import com.sondercafe.app.data.local.dao.UserDao
import com.sondercafe.app.data.local.entity.UserEntity
import com.sondercafe.app.domain.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Fills an empty database with the menu and, in debug builds, a demo admin account whose
 * credentials come from local.properties. Safe to run on every launch.
 */
class DatabaseSeeder @Inject constructor(
    private val menuDao: MenuDao,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) {

    suspend fun seed() {
        if (menuDao.count() == 0) {
            menuDao.insertAll(MenuSeedData.items)
        }
        seedAdmin()
    }

    private suspend fun seedAdmin() {
        val email = InputSanitizer.email(BuildConfig.ADMIN_SEED_EMAIL)
        val password = BuildConfig.ADMIN_SEED_PASSWORD
        if (email.isEmpty() || password.isEmpty()) return

        if (Validators.email(email) != null || Validators.password(password) != null) {
            // Never log the password itself.
            Log.w(TAG, "Admin seed skipped: ADMIN_SEED_EMAIL or ADMIN_SEED_PASSWORD doesn't meet the validation rules")
            return
        }
        if (userDao.findByEmail(email) != null) return

        val hashed = withContext(Dispatchers.Default) { passwordHasher.hash(password) }
        try {
            userDao.insert(
                UserEntity(
                    email = email,
                    displayName = "Cafe Admin",
                    passwordHash = hashed.hash,
                    passwordSalt = hashed.salt,
                    role = Role.ADMIN,
                    createdAt = Instant.now()
                )
            )
        } catch (e: SQLiteConstraintException) {
            // Another launch got there first. Nothing to do.
        }
    }

    private companion object {
        const val TAG = "DatabaseSeeder"
    }
}
