package com.sondercafe.app.testutil

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderLine
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.model.totalPence
import com.sondercafe.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/**
 * In-memory orders. If a [FakeCartRepository] is given, placing an order empties that cart,
 * like the real transaction does.
 */
class FakeOrderRepository(
    private val cartRepository: FakeCartRepository? = null
) : OrderRepository {

    private val orders = MutableStateFlow<List<Order>>(emptyList())
    private var nextId = 1L

    var placeOrderError: AppResult.Error? = null
    var personalCounts: Map<Long, Int> = emptyMap()
    var globalCounts: Map<Long, Int> = emptyMap()

    val allOrders: List<Order> get() = orders.value

    override suspend fun placeOrder(userId: Long, lines: List<CartLine>): AppResult<Long> {
        placeOrderError?.let { return it }
        val now = Instant.now()
        val order = Order(
            id = nextId++,
            userId = userId,
            status = OrderStatus.PENDING,
            lines = lines.map { OrderLine(it.menuItem.id, it.menuItem.name, it.menuItem.pricePence, it.quantity) },
            totalPence = lines.totalPence(),
            createdAt = now,
            updatedAt = now
        )
        orders.update { it + order }
        cartRepository?.clear(userId)
        return AppResult.Success(order.id)
    }

    override fun observeOrders(userId: Long): Flow<List<Order>> =
        orders.map { list -> list.filter { it.userId == userId }.sortedByDescending { it.createdAt } }

    override fun observeOrder(orderId: Long): Flow<Order?> = orders.map { list -> list.find { it.id == orderId } }

    override fun observeAllOrders(): Flow<List<Order>> = orders.map { it.sortedByDescending { order -> order.createdAt } }

    override suspend fun getOrder(orderId: Long): Order? = orders.value.find { it.id == orderId }

    override suspend fun updateStatus(orderId: Long, newStatus: OrderStatus): AppResult<Order> {
        val order = getOrder(orderId) ?: return AppResult.Error("Order not found")
        if (!order.status.canMoveTo(newStatus)) {
            return AppResult.Error("Can't move an order from ${order.status.label} to ${newStatus.label}")
        }
        val updated = order.copy(status = newStatus, updatedAt = Instant.now())
        orders.update { list -> list.map { if (it.id == orderId) updated else it } }
        return AppResult.Success(updated)
    }

    override suspend fun getItemCountsForUser(userId: Long): Map<Long, Int> = personalCounts

    override suspend fun getItemCountsForAllUsers(): Map<Long, Int> = globalCounts
}
