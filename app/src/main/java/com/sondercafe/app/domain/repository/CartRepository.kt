package com.sondercafe.app.domain.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun observeCart(userId: Long): Flow<List<CartLine>>

    suspend fun getCart(userId: Long): List<CartLine>

    /** Adds to the existing quantity if the item is already in the cart. */
    suspend fun addItem(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit>

    suspend fun updateQuantity(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit>

    suspend fun removeItem(userId: Long, menuItemId: Long): AppResult<Unit>
}
