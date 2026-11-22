package com.sondercafe.app.domain.usecase

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.repository.CartRepository
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.domain.service.OrderNotifier
import com.sondercafe.app.domain.service.OrderSimulator
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Turns the customer's cart into an order. The cart is re-read from the database here rather
 * than trusted from the screen, so prices and availability are checked at the moment of ordering.
 */
class PlaceOrderUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val orderSimulator: OrderSimulator,
    private val orderNotifier: OrderNotifier
) {

    suspend operator fun invoke(userId: Long): AppResult<Long> {
        val cart = try {
            cartRepository.getCart(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return AppResult.Error("Couldn't load your cart. Please try again.", e)
        }

        if (cart.isEmpty()) {
            return AppResult.Error("Your cart is empty")
        }

        val unavailable = cart.filterNot { it.menuItem.isAvailable }.map { it.menuItem.name }
        if (unavailable.isNotEmpty()) {
            val names = unavailable.joinToString(", ")
            val (verb, pronoun) = if (unavailable.size == 1) "is" to "it" else "are" to "them"
            return AppResult.Error("$names $verb no longer available. Remove $pronoun from your cart to continue.")
        }

        cart.firstNotNullOfOrNull { Validators.quantity(it.quantity) }?.let { message ->
            return AppResult.Error(message)
        }

        val result = orderRepository.placeOrder(userId, cart)
        if (result is AppResult.Success) {
            orderNotifier.notifyStatusChanged(result.data, OrderStatus.PENDING)
            orderSimulator.startProcessing(result.data)
        }
        return result
    }
}
