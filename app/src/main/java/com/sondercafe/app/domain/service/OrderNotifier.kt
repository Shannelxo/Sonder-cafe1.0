package com.sondercafe.app.domain.service

import com.sondercafe.app.domain.model.OrderStatus

/**
 * Tells the customer their order has changed. The domain layer only knows this interface; the
 * Android notification code lives in core/notifications, so business logic stays testable.
 */
interface OrderNotifier {
    fun notifyStatusChanged(orderId: Long, status: OrderStatus)
}
