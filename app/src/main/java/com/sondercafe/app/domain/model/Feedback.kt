package com.sondercafe.app.domain.model

import java.time.Instant

/** A customer's rating of a collected order. One per order. */
data class Feedback(
    val id: Long,
    val userId: Long,
    val orderId: Long,
    val rating: Int,
    val comment: String,
    val createdAt: Instant
)
