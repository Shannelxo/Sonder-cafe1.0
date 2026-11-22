package com.sondercafe.app.domain.model

/** One menu item in the cart. The price comes from the live menu, so the cart always shows current prices. */
data class CartLine(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val lineTotalPence: Int get() = menuItem.pricePence * quantity
}

fun List<CartLine>.totalPence(): Int = sumOf { it.lineTotalPence }

fun List<CartLine>.itemCount(): Int = sumOf { it.quantity }
