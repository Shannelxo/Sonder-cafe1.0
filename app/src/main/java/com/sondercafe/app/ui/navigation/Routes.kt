package com.sondercafe.app.ui.navigation

/**
 * Navigation routes. IDs are passed as path arguments and read back in the ViewModels through
 * SavedStateHandle, which keeps the ViewModels easy to unit test (no Bundle needed).
 */
object Routes {

    const val ARG_ITEM_ID = "itemId"
    const val ARG_ORDER_ID = "orderId"

    const val LOGIN = "login"
    const val REGISTER = "register"

    const val MENU = "menu"
    const val MENU_ITEM = "menu/{$ARG_ITEM_ID}"
    const val CART = "cart"

    const val ORDERS = "orders"
    const val ORDER_DETAIL = "orders/{$ARG_ORDER_ID}"
    const val ORDER_CONFIRMATION = "orders/{$ARG_ORDER_ID}/confirmation"
    const val FEEDBACK = "orders/{$ARG_ORDER_ID}/feedback"

    const val ACCOUNT = "account"
    const val ADMIN = "admin"

    fun menuItem(itemId: Long) = "menu/$itemId"
    fun orderDetail(orderId: Long) = "orders/$orderId"
    fun orderConfirmation(orderId: Long) = "orders/$orderId/confirmation"
    fun feedback(orderId: Long) = "orders/$orderId/feedback"
}
