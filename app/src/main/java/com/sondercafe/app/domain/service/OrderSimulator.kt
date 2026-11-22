package com.sondercafe.app.domain.service

/**
 * Stands in for café staff in this customer-only app. Once started, it moves an order through
 * each status in turn using the same repository and rules as a real status update.
 */
interface OrderSimulator {
    fun startProcessing(orderId: Long)
}
