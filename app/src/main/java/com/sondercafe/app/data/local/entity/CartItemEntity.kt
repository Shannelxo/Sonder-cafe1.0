package com.sondercafe.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.Instant

/**
 * One row per (user, menu item). The composite key means the same item can't appear twice in
 * a cart; adding it again increases the quantity instead.
 */
@Entity(
    tableName = "cart_items",
    primaryKeys = ["user_id", "menu_item_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["menu_item_id"])]
)
data class CartItemEntity(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long,
    val quantity: Int,
    @ColumnInfo(name = "added_at") val addedAt: Instant
)
