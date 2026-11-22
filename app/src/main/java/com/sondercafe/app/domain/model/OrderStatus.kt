package com.sondercafe.app.domain.model

/**
 * The order workflow. Orders only ever move forward one step at a time, which keeps the
 * simulated processing and the admin console following the same rules.
 */
enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY_FOR_COLLECTION("Ready for collection"),
    COLLECTED("Collected");

    val isActive: Boolean get() = this != COLLECTED

    fun next(): OrderStatus? = entries.getOrNull(ordinal + 1)

    fun canMoveTo(target: OrderStatus): Boolean = next() == target
}
