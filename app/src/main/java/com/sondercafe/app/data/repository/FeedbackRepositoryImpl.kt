package com.sondercafe.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.InputSanitizer
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.data.local.dao.FeedbackDao
import com.sondercafe.app.data.local.dao.OrderDao
import com.sondercafe.app.data.local.entity.FeedbackEntity
import com.sondercafe.app.data.mapper.toDomain
import com.sondercafe.app.domain.model.Feedback
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.repository.FeedbackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val feedbackDao: FeedbackDao,
    private val orderDao: OrderDao
) : FeedbackRepository {

    override suspend fun submitFeedback(userId: Long, orderId: Long, rating: Int, comment: String): AppResult<Unit> {
        val cleanComment = InputSanitizer.multiLine(comment, Validators.COMMENT_MAX_LENGTH)
        val validationError = Validators.rating(rating) ?: Validators.comment(cleanComment)
        if (validationError != null) return AppResult.Error(validationError)

        return try {
            val order = orderDao.getOrder(orderId)?.order
            when {
                order == null -> AppResult.Error("Order not found")
                order.userId != userId -> AppResult.Error("You can only rate your own orders")
                order.status != OrderStatus.COLLECTED -> AppResult.Error("You can rate this order once it has been collected")
                feedbackDao.findForOrder(orderId) != null -> AppResult.Error(ALREADY_RATED)
                else -> {
                    feedbackDao.insert(
                        FeedbackEntity(
                            userId = userId,
                            orderId = orderId,
                            rating = rating,
                            comment = cleanComment,
                            createdAt = Instant.now()
                        )
                    )
                    AppResult.Success(Unit)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteConstraintException) {
            AppResult.Error(ALREADY_RATED, e)
        } catch (e: Exception) {
            AppResult.Error("Couldn't send your feedback. Please try again.", e)
        }
    }

    override fun observeFeedbackForOrder(orderId: Long): Flow<Feedback?> =
        feedbackDao.observeForOrder(orderId).map { it?.toDomain() }

    override suspend fun getAverageRatings(): Map<Long, Double> =
        feedbackDao.getAverageRatings().associate { it.menuItemId to it.averageRating }

    private companion object {
        const val ALREADY_RATED = "You've already rated this order"
    }
}
