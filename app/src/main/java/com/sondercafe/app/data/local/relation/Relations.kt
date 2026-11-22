package com.sondercafe.app.data.local.relation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.local.entity.MenuItemEntity
import com.sondercafe.app.data.local.entity.OrderEntity
import com.sondercafe.app.data.local.entity.OrderItemEntity

// Types returned by DAO queries that combine or aggregate tables.

data class CartItemWithMenuItem(
    @Embedded val cartItem: CartItemEntity,
    @Relation(parentColumn = "menu_item_id", entityColumn = "id")
    val menuItem: MenuItemEntity
)

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "order_id")
    val items: List<OrderItemEntity>
)

data class ItemCount(
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long,
    val total: Int
)

data class ItemRating(
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long,
    @ColumnInfo(name = "average_rating") val averageRating: Double
)
