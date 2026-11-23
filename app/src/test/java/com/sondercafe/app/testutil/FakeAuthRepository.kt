package com.sondercafe.app.testutil

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.Role
import com.sondercafe.app.domain.model.User
import com.sondercafe.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory AuthRepository. Passwords are kept in plain text, which is fine for a test double
 * and nowhere else.
 */
class FakeAuthRepository : AuthRepository {

    private val accounts = mutableMapOf<String, Pair<User, String>>()
    private val session = MutableStateFlow<User?>(null)
    private var nextId = 1L

    /** When set, login fails with this message regardless of the credentials. */
    var loginError: String? = null

    var loginCalls = 0
        private set
    var registerCalls = 0
        private set

    override val currentUser: Flow<User?> = session

    fun addAccount(email: String, password: String, displayName: String = "Sam", role: Role = Role.CUSTOMER): User {
        val user = User(id = nextId++, email = email, displayName = displayName, role = role)
        accounts[email] = user to password
        return user
    }

    fun signIn(user: User) {
        session.value = user
    }

    override suspend fun register(displayName: String, email: String, password: String): AppResult<User> {
        registerCalls++
        if (email in accounts) return AppResult.Error("An account with this email already exists")
        val user = addAccount(email, password, displayName)
        session.value = user
        return AppResult.Success(user)
    }

    override suspend fun login(email: String, password: String): AppResult<User> {
        loginCalls++
        loginError?.let { return AppResult.Error(it) }
        val account = accounts[email]
        if (account == null || account.second != password) {
            return AppResult.Error("Email or password is incorrect")
        }
        session.value = account.first
        return AppResult.Success(account.first)
    }

    override suspend fun logout() {
        session.value = null
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        val user = session.value ?: return AppResult.Error("You're not logged in")
        accounts.remove(user.email)
        session.value = null
        return AppResult.Success(Unit)
    }
}
