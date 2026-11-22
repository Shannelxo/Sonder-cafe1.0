package com.sondercafe.app.domain.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** The signed-in user, or null when nobody is logged in. Survives app restarts. */
    val currentUser: Flow<User?>

    /** Always creates a customer account. Admin accounts can't be created from the app. */
    suspend fun register(displayName: String, email: String, password: String): AppResult<User>

    suspend fun login(email: String, password: String): AppResult<User>

    suspend fun logout()

    /** Deletes the signed-in user along with their cart, orders and feedback, then logs out. */
    suspend fun deleteAccount(): AppResult<Unit>
}
