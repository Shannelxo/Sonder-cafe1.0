package com.sondercafe.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sondercafe.app.data.local.entity.FeedbackEntity
import com.sondercafe.app.data.local.relation.ItemRating
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {

    /** Throws SQLiteConstraintException if the order has already been rated. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(feedback: FeedbackEntity): Long

    @Query("SELECT * FROM feedback WHERE order_id = :orderId")
    fun observeForOrder(orderId: Long): Flow<FeedbackEntity?>

    @Query("SELECT * FROM feedback WHERE order_id = :orderId")
    suspend fun findForOrder(orderId: Long): FeedbackEntity?

    /** Each order's rating counts towards every item that was in that order. */
    @Query(
        """
        SELECT oi.menu_item_id AS menu_item_id, AVG(f.rating) AS average_rating
        FROM feedback f
        INNER JOIN order_items oi ON oi.order_id = f.order_id
        GROUP BY oi.menu_item_id
        """
    )
    suspend fun getAverageRatings(): List<ItemRating>
}
