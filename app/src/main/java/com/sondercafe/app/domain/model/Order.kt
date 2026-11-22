package com.sondercafe.app.domain.model

import java.time.Instant

/**
 * A line in a placed order. Name and price are copied when the order is placed, so past
 * orders still show what the customer actually paid even if the menu changes later.
 */
data class OrderLine(
    val menuItemId: Long,
    val name: String,
    val unitPricePence: Int,
    val quantity: Int
) {
    val lineTotalPence: Int get() = unitPricePence * quantity
}

data class Order(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val lines: List<OrderLine>,
    val totalPence: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
}
