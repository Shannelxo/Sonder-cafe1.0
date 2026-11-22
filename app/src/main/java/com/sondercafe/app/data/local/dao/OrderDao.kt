package com.sondercafe.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.sondercafe.app.data.local.entity.OrderEntity
import com.sondercafe.app.data.local.entity.OrderItemEntity
import com.sondercafe.app.data.local.relation.ItemCount
import com.sondercafe.app.data.local.relation.OrderWithItems
import com.sondercafe.app.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
abstract class OrderDao {

    // The three building blocks below are only meant to be called through placeOrder().

    @Insert
    abstract suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    abstract suspend fun insertItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    abstract suspend fun clearCart(userId: Long)

    /**
     * Saves the order and its lines and empties the cart in one transaction. If any step
     * throws, Room rolls back all of them.
     */
    @Transaction
    open suspend fun placeOrder(order: OrderEntity, items: List<OrderItemEntity>): Long {
        val orderId = insertOrder(order)
        insertItems(items.map { it.copy(orderId = orderId) })
        clearCart(order.userId)
        return orderId
    }

    @Transaction
    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
    abstract fun observeOrdersForUser(userId: Long): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    abstract fun observeOrder(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    abstract fun observeAllOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    abstract suspend fun getOrder(orderId: Long): OrderWithItems?

    /**
     * Only updates if the order is still in [expectedStatus]. If the simulator and the admin
     * console try to move the same order at once, only the first succeeds; the other gets 0
     * rows back instead of overwriting.
     */
    @Query(
        """
        UPDATE orders SET status = :newStatus, updated_at = :updatedAt
        WHERE id = :orderId AND status = :expectedStatus
        """
    )
    abstract suspend fun updateStatus(
        orderId: Long,
        expectedStatus: OrderStatus,
        newStatus: OrderStatus,
        updatedAt: Instant
    ): Int

    @Query(
        """
        SELECT oi.menu_item_id AS menu_item_id, SUM(oi.quantity) AS total
        FROM order_items oi
        INNER JOIN orders o ON o.id = oi.order_id
        WHERE o.user_id = :userId
        GROUP BY oi.menu_item_id
        """
    )
    abstract suspend fun getItemCountsForUser(userId: Long): List<ItemCount>

    @Query(
        """
        SELECT menu_item_id, SUM(quantity) AS total
        FROM order_items
        GROUP BY menu_item_id
        """
    )
    abstract suspend fun getItemCountsForAllUsers(): List<ItemCount>
}
