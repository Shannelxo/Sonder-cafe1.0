package com.sondercafe.app.data.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.data.local.dao.OrderDao
import com.sondercafe.app.data.local.entity.OrderEntity
import com.sondercafe.app.data.mapper.toDomain
import com.sondercafe.app.data.mapper.toOrderItemEntity
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.model.totalPence
import com.sondercafe.app.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override suspend fun placeOrder(userId: Long, lines: List<CartLine>): AppResult<Long> = try {
        val now = Instant.now()
        val order = OrderEntity(
            userId = userId,
            status = OrderStatus.PENDING,
            totalPence = lines.totalPence(),
            createdAt = now,
            updatedAt = now
        )
        AppResult.Success(orderDao.placeOrder(order, lines.map { it.toOrderItemEntity() }))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error("Couldn't place your order. Your cart hasn't been changed, so please try again.", e)
    }

    override fun observeOrders(userId: Long): Flow<List<Order>> =
        orderDao.observeOrdersForUser(userId).map { orders -> orders.map { it.toDomain() } }

    override fun observeOrder(orderId: Long): Flow<Order?> =
        orderDao.observeOrder(orderId).map { it?.toDomain() }

    override fun observeAllOrders(): Flow<List<Order>> =
        orderDao.observeAllOrders().map { orders -> orders.map { it.toDomain() } }

    override suspend fun getOrder(orderId: Long): Order? = orderDao.getOrder(orderId)?.toDomain()

    override suspend fun updateStatus(orderId: Long, newStatus: OrderStatus): AppResult<Order> = try {
        val current = orderDao.getOrder(orderId)?.order?.status
        when {
            current == null -> AppResult.Error("Order not found")
            !current.canMoveTo(newStatus) ->
                AppResult.Error("This order can't move from ${current.label} to ${newStatus.label}")
            orderDao.updateStatus(orderId, current, newStatus, Instant.now()) == 0 ->
                AppResult.Error("This order was just updated. Please check its status and try again.")
            else -> AppResult.Success(orderDao.getOrder(orderId)!!.toDomain())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error("Couldn't update the order status", e)
    }

    override suspend fun getItemCountsForUser(userId: Long): Map<Long, Int> =
        orderDao.getItemCountsForUser(userId).associate { it.menuItemId to it.total }

    override suspend fun getItemCountsForAllUsers(): Map<Long, Int> =
        orderDao.getItemCountsForAllUsers().associate { it.menuItemId to it.total }
}
