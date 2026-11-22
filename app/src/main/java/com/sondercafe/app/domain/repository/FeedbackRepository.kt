package com.sondercafe.app.domain.repository

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.Feedback
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {

    /** Only collected orders belonging to [userId] can be rated, and only once. */
    suspend fun submitFeedback(userId: Long, orderId: Long, rating: Int, comment: String): AppResult<Unit>

    fun observeFeedbackForOrder(orderId: Long): Flow<Feedback?>

    /**
     * Average rating for each menu item, based on the ratings of the orders it appeared in.
     * Keyed by menu item id; items with no ratings are left out.
     */
    suspend fun getAverageRatings(): Map<Long, Double>
}
