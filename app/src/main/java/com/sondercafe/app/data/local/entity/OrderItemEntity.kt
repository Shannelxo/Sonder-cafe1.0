package com.sondercafe.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A line in a placed order. Name and price are copied from the menu at the time of ordering.
 *
 * menu_item_id deliberately has no foreign key: if an item is later removed from the menu, the
 * customer's order history should still show it rather than lose the line.
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["order_id"]), Index(value = ["menu_item_id"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long,
    @ColumnInfo(name = "name_snapshot") val nameSnapshot: String,
    @ColumnInfo(name = "unit_price_pence") val unitPricePence: Int,
    val quantity: Int
)
