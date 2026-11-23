package com.sondercafe.app.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.domain.service.OrderNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Moves one order to one target status, the way a member of staff would. The simulator chains
 * several of these together so an order goes through the whole workflow.
 */
@HiltWorker
class OrderProgressWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val orderRepository: OrderRepository,
    private val orderNotifier: OrderNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getLong(KEY_ORDER_ID, -1)
        val target = inputData.getString(KEY_TARGET_STATUS)
            ?.let { name -> OrderStatus.entries.find { it.name == name } }
        if (orderId < 0 || target == null) return Result.failure()

        // The order may have been deleted along with its account. Nothing left to do.
        val order = orderRepository.getOrder(orderId) ?: return Result.success()

        // Already there or past it, for example because an admin moved it on by hand.
        if (order.status.ordinal >= target.ordinal) return Result.success()

        return when (orderRepository.updateStatus(orderId, target)) {
            is AppResult.Success -> {
                orderNotifier.notifyStatusChanged(orderId, target)
                Result.success()
            }
            is AppResult.Error -> if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_ORDER_ID = "order_id"
        const val KEY_TARGET_STATUS = "target_status"
        private const val MAX_ATTEMPTS = 3

        fun inputFor(orderId: Long, target: OrderStatus) =
            workDataOf(KEY_ORDER_ID to orderId, KEY_TARGET_STATUS to target.name)
    }
}
