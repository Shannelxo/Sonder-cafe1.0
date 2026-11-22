package com.sondercafe.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.local.relation.CartItemWithMenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    // @Transaction makes the cart rows and their menu items load as one consistent read.
    @Transaction
    @Query("SELECT * FROM cart_items WHERE user_id = :userId ORDER BY added_at")
    fun observeCart(userId: Long): Flow<List<CartItemWithMenuItem>>

    @Transaction
    @Query("SELECT * FROM cart_items WHERE user_id = :userId ORDER BY added_at")
    suspend fun getCart(userId: Long): List<CartItemWithMenuItem>

    @Query("SELECT * FROM cart_items WHERE user_id = :userId AND menu_item_id = :menuItemId")
    suspend fun find(userId: Long, menuItemId: Long): CartItemEntity?

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE user_id = :userId AND menu_item_id = :menuItemId")
    suspend fun updateQuantity(userId: Long, menuItemId: Long, quantity: Int): Int

    @Query("DELETE FROM cart_items WHERE user_id = :userId AND menu_item_id = :menuItemId")
    suspend fun delete(userId: Long, menuItemId: Long): Int
}
