package com.sondercafe.app.domain.model

/**
 * A suggested menu item. [reason] is shown to the customer so they can see why the item was
 * suggested, rather than the app nudging them without explanation.
 */
data class Recommendation(
    val menuItem: MenuItem,
    val score: Double,
    val reason: String
)
