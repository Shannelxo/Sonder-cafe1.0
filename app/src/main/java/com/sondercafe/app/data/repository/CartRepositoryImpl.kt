package com.sondercafe.app.data.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.data.local.dao.CartDao
import com.sondercafe.app.data.local.dao.MenuDao
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.mapper.toDomain
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.repository.CartRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val menuDao: MenuDao
) : CartRepository {

    override fun observeCart(userId: Long): Flow<List<CartLine>> =
        cartDao.observeCart(userId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getCart(userId: Long): List<CartLine> =
        cartDao.getCart(userId).map { it.toDomain() }

    override suspend fun addItem(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit> {
        Validators.quantity(quantity)?.let { return AppResult.Error(it) }

        return safely("Couldn't add this item to your cart") {
            val item = menuDao.findById(menuItemId)
                ?: return@safely AppResult.Error("This item is no longer on the menu")
            if (!item.isAvailable) {
                return@safely AppResult.Error("${item.name} is currently unavailable")
            }

            val existing = cartDao.find(userId, menuItemId)
            val newQuantity = (existing?.quantity ?: 0) + quantity
            if (newQuantity > Validators.MAX_QUANTITY) {
                return@safely AppResult.Error("You can have up to ${Validators.MAX_QUANTITY} of each item in your cart")
            }

            cartDao.upsert(
                CartItemEntity(
                    userId = userId,
                    menuItemId = menuItemId,
                    quantity = newQuantity,
                    // Keep the original position in the cart when increasing the quantity.
                    addedAt = existing?.addedAt ?: Instant.now()
                )
            )
            AppResult.Success(Unit)
        }
    }

    override suspend fun updateQuantity(userId: Long, menuItemId: Long, quantity: Int): AppResult<Unit> {
        Validators.quantity(quantity)?.let { return AppResult.Error(it) }

        return safely("Couldn't update the quantity") {
            val rows = cartDao.updateQuantity(userId, menuItemId, quantity)
            if (rows == 0) AppResult.Error("This item is no longer in your cart") else AppResult.Success(Unit)
        }
    }

    override suspend fun removeItem(userId: Long, menuItemId: Long): AppResult<Unit> =
        safely("Couldn't remove this item") {
            cartDao.delete(userId, menuItemId)
            AppResult.Success(Unit)
        }

    private inline fun <T> safely(errorMessage: String, block: () -> AppResult<T>): AppResult<T> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(errorMessage, e)
        }
}
