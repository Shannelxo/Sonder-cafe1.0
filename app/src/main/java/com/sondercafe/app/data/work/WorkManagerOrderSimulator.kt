package com.sondercafe.app.data.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.service.OrderSimulator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simulates the café processing an order by chaining one worker per status change. WorkManager
 * keeps the chain going if the app is closed, and each step runs only after the previous one
 * finishes, so the order always moves through the statuses in the right order.
 */
@Singleton
class WorkManagerOrderSimulator @Inject constructor(
    @ApplicationContext private val context: Context
) : OrderSimulator {

    override fun startProcessing(orderId: Long) {
        val steps = listOf(
            step(orderId, OrderStatus.PREPARING, PREPARING_DELAY),
            step(orderId, OrderStatus.READY_FOR_COLLECTION, READY_DELAY),
            step(orderId, OrderStatus.COLLECTED, COLLECTED_DELAY)
        )

        WorkManager.getInstance(context)
            // Unique per order, so placing an order twice by accident can't start two chains.
            .beginUniqueWork("order-$orderId", ExistingWorkPolicy.KEEP, steps.first())
            .then(steps[1])
            .then(steps[2])
            .enqueue()
    }

    private fun step(orderId: Long, target: OrderStatus, delay: Duration): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<OrderProgressWorker>()
            .setInitialDelay(delay)
            .setInputData(OrderProgressWorker.inputFor(orderId, target))
            .addTag(TAG)
            .build()

    companion object {
        const val TAG = "order-simulation"

        // Short delays so a full order can be shown in a demo. A real café would take longer.
        val PREPARING_DELAY: Duration = Duration.ofSeconds(15)
        val READY_DELAY: Duration = Duration.ofSeconds(30)
        val COLLECTED_DELAY: Duration = Duration.ofSeconds(45)
    }
}
