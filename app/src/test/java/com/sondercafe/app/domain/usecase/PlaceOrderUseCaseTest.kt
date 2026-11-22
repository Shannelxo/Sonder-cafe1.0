package com.sondercafe.app.domain.usecase

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.service.OrderNotifier
import com.sondercafe.app.domain.service.OrderSimulator
import com.sondercafe.app.testutil.FakeCartRepository
import com.sondercafe.app.testutil.FakeOrderRepository
import com.sondercafe.app.testutil.sampleMenuItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaceOrderUseCaseTest {

    private class RecordingSimulator : OrderSimulator {
        val started = mutableListOf<Long>()
        override fun startProcessing(orderId: Long) {
            started += orderId
        }
    }

    private class RecordingNotifier : OrderNotifier {
        val sent = mutableListOf<Pair<Long, OrderStatus>>()
        override fun notifyStatusChanged(orderId: Long, status: OrderStatus) {
            sent += orderId to status
        }
    }

    private val userId = 7L
    private val latte = sampleMenuItem(id = 1, name = "Latte", pricePence = 350)
    private val scone = sampleMenuItem(id = 2, name = "Scone", pricePence = 275)

    private lateinit var cart: FakeCartRepository
    private lateinit var orders: FakeOrderRepository
    private lateinit var simulator: RecordingSimulator
    private lateinit var notifier: RecordingNotifier
    private lateinit var placeOrder: PlaceOrderUseCase

    @Before
    fun setUp() {
        cart = FakeCartRepository(listOf(latte, scone))
        orders = FakeOrderRepository(cart)
        simulator = RecordingSimulator()
        notifier = RecordingNotifier()
        placeOrder = PlaceOrderUseCase(cart, orders, simulator, notifier)
    }

    @Test
    fun `places an order, empties the cart and starts processing`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 2), CartLine(scone, 1)))

        val result = placeOrder(userId)

        assertTrue(result is AppResult.Success)
        val orderId = (result as AppResult.Success).data
        val order = orders.getOrder(orderId)!!
        assertEquals(975, order.totalPence)
        assertEquals(OrderStatus.PENDING, order.status)
        assertTrue(cart.cartFor(userId).isEmpty())
        assertEquals(listOf(orderId), simulator.started)
        assertEquals(listOf(orderId to OrderStatus.PENDING), notifier.sent)
    }

    @Test
    fun `refuses an empty cart`() = runTest {
        val result = placeOrder(userId)

        assertEquals("Your cart is empty", (result as AppResult.Error).message)
        assertTrue(orders.allOrders.isEmpty())
        assertTrue(simulator.started.isEmpty())
    }

    @Test
    fun `refuses a cart containing an unavailable item`() = runTest {
        val soldOut = scone.copy(isAvailable = false)
        cart.setCart(userId, listOf(CartLine(latte, 1), CartLine(soldOut, 1)))

        val result = placeOrder(userId)

        assertTrue((result as AppResult.Error).message.startsWith("Scone is no longer available"))
        assertTrue(orders.allOrders.isEmpty())
    }

    @Test
    fun `refuses an out-of-range quantity`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 50)))

        val result = placeOrder(userId)

        assertTrue(result is AppResult.Error)
        assertTrue(orders.allOrders.isEmpty())
    }

    @Test
    fun `passes on a repository failure without starting processing`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1)))
        orders.placeOrderError = AppResult.Error("Couldn't place your order")

        val result = placeOrder(userId)

        assertEquals("Couldn't place your order", (result as AppResult.Error).message)
        assertTrue(simulator.started.isEmpty())
        assertTrue(notifier.sent.isEmpty())
        assertEquals(1, cart.cartFor(userId).size)
    }

    @Test
    fun `turns a cart loading failure into a friendly error`() = runTest {
        cart.failGetCart = true

        val result = placeOrder(userId)

        assertEquals("Couldn't load your cart. Please try again.", (result as AppResult.Error).message)
    }
}
