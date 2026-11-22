package com.sondercafe.app.testutil

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory cart that follows the same rules as the real repository. */
class FakeCartRepository(menu: List<MenuItem> = emptyList()) : CartRepository {

    private val menuById = menu.associateBy { it.id }.toMutableMap()
    private val carts = MutableStateFlow<Map<Long, List<CartLine>>>(emptyMap())

    var failGetCart = false

    fun setCart(userId: Long, lines: List<CartLine>) {
        lines.forEach { menuById[it.menuItem.id] = it.menuItem }
        carts.update { it + (userId to lines) }
    }

    fun clear(userId: Long) {
        carts.update { it - userId }
    }

    fun cartFor(userId: Long): List<CartLine> = carts.value[userId].orEmpty()

    override fun observeCart(userId: Long): Flow<List<CartLine>> = carts.map { it[userId].orEmpty() }

    override suspend fun getCart(userId: Long): List<CartLine> {
        if (failGetCart) throw IllegalStateException("Simulated database failure")
        return cartFor(userId)
    }

    override suspend fun addItem(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit> {
        val item = menuById[menuItemId] ?: return AppResult.Error("This item is no longer on the menu")
        if (!item.isAvailable) return AppResult.Error("${item.name} is currently unavailable")
        val existing = cartFor(userId).find { it.menuItem.id == menuItemId }
        val newQuantity = (existing?.quantity ?: 0) + quantity
        Validators.quantity(newQuantity)?.let { return AppResult.Error(it) }

        val others = cartFor(userId).filterNot { it.menuItem.id == menuItemId }
        setCart(userId, others + CartLine(item, newQuantity))
        return AppResult.Success(Unit)
    }

    override suspend fun updateQuantity(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit> {
        Validators.quantity(quantity)?.let { return AppResult.Error(it) }
        setCart(userId, cartFor(userId).map { if (it.menuItem.id == menuItemId) it.copy(quantity = quantity) else it })
        return AppResult.Success(Unit)
    }

    override suspend fun removeItem(userId: Long, menuItemId: Long): AppResult<Unit> {
        setCart(userId, cartFor(userId).filterNot { it.menuItem.id == menuItemId })
        return AppResult.Success(Unit)
    }
}
