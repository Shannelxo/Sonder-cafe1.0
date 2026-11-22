package com.sondercafe.app.domain.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {

    /**
     * Saves the order and its lines and empties the user's cart in a single transaction, so a
     * failure part-way can't leave an order without items or a cart that was charged twice.
     * Returns the new order id.
     */
    suspend fun placeOrder(userId: Long, lines: List<CartLine>): AppResult<Long>

    fun observeOrders(userId: Long): Flow<List<Order>>

    fun observeOrder(orderId: Long): Flow<Order?>

    /** Every customer's orders, newest first. Only for the admin console. */
    fun observeAllOrders(): Flow<List<Order>>

    suspend fun getOrder(orderId: Long): Order?

    /** Fails if the move isn't allowed by [OrderStatus.canMoveTo]. Returns the updated order. */
    suspend fun updateStatus(orderId: Long, newStatus: OrderStatus): AppResult<Order>

    /** How many of each menu item this user has ordered, keyed by menu item id. */
    suspend fun getItemCountsForUser(userId: Long): Map<Long, Int>

    /** How many of each menu item has been ordered by all customers, keyed by menu item id. */
    suspend fun getItemCountsForAllUsers(): Map<Long, Int>
}
