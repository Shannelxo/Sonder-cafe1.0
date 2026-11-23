package com.sondercafe.app.ui.cart

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.service.OrderNotifier
import com.sondercafe.app.domain.service.OrderSimulator
import com.sondercafe.app.domain.usecase.PlaceOrderUseCase
import com.sondercafe.app.testutil.FakeAuthRepository
import com.sondercafe.app.testutil.FakeCartRepository
import com.sondercafe.app.testutil.FakeOrderRepository
import com.sondercafe.app.testutil.MainDispatcherRule
import com.sondercafe.app.testutil.sampleMenuItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val latte = sampleMenuItem(id = 1, name = "Latte", pricePence = 350)
    private val scone = sampleMenuItem(id = 2, name = "Scone", pricePence = 275)
    private val brownie = sampleMenuItem(id = 3, name = "Brownie", available = false)

    private lateinit var auth: FakeAuthRepository
    private lateinit var cart: FakeCartRepository
    private lateinit var orders: FakeOrderRepository
    private val started = mutableListOf<Long>()
    private var userId = 0L

    @Before
    fun setUp() {
        auth = FakeAuthRepository()
        val user = auth.addAccount("sam@example.com", "coffee123")
        auth.signIn(user)
        userId = user.id
        cart = FakeCartRepository(listOf(latte, scone, brownie))
        orders = FakeOrderRepository(cart)
    }

    private fun createViewModel(): CartViewModel {
        val simulator = object : OrderSimulator {
            override fun startProcessing(orderId: Long) {
                started += orderId
            }
        }
        val notifier = object : OrderNotifier {
            override fun notifyStatusChanged(orderId: Long, status: OrderStatus) = Unit
        }
        return CartViewModel(cart, auth, PlaceOrderUseCase(cart, orders, simulator, notifier))
    }

    /** uiState only updates while something collects it, as the screen would. */
    private fun TestScope.collect(viewModel: CartViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `shows cart lines with totals`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 2), CartLine(scone, 1)))
        val viewModel = createViewModel()
        collect(viewModel)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.lines.size)
        assertEquals(975, state.totalPence)
        assertEquals(3, state.itemCount)
        assertTrue(state.canPlaceOrder)
    }

    @Test
    fun `changing quantity updates the total`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1)))
        val viewModel = createViewModel()
        collect(viewModel)

        viewModel.changeQuantity(latte.id, 3)

        assertEquals(1050, viewModel.uiState.value.totalPence)
    }

    @Test
    fun `quantity over the limit shows the repository error`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1)))
        val viewModel = createViewModel()
        collect(viewModel)

        viewModel.changeQuantity(latte.id, 25)

        assertEquals("Quantity must be between 1 and 20", viewModel.uiState.value.errorMessage)
        assertEquals(1, viewModel.uiState.value.lines.single().quantity)
    }

    @Test
    fun `removing an item takes it out of the cart`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1), CartLine(scone, 1)))
        val viewModel = createViewModel()
        collect(viewModel)

        viewModel.removeItem(latte.id)

        assertEquals(listOf(scone.id), viewModel.uiState.value.lines.map { it.menuItem.id })
    }

    @Test
    fun `placing an order empties the cart and reports the order id`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 2)))
        val viewModel = createViewModel()
        collect(viewModel)

        viewModel.placeOrder()

        val state = viewModel.uiState.value
        val orderId = state.placedOrderId
        assertEquals(orders.allOrders.single().id, orderId)
        assertTrue(state.lines.isEmpty())
        assertEquals(listOf(orderId), started)

        viewModel.onOrderPlacedHandled()
        assertNull(viewModel.uiState.value.placedOrderId)
    }

    @Test
    fun `unavailable item blocks checkout`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1), CartLine(brownie, 1)))
        val viewModel = createViewModel()
        collect(viewModel)

        val state = viewModel.uiState.value
        assertTrue(state.hasUnavailableItems)
        assertFalse(state.canPlaceOrder)
    }

    @Test
    fun `failed order shows the error and keeps the cart`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1)))
        orders.placeOrderError = AppResult.Error("Couldn't place your order. Please try again.")
        val viewModel = createViewModel()
        collect(viewModel)

        viewModel.placeOrder()

        val state = viewModel.uiState.value
        assertEquals("Couldn't place your order. Please try again.", state.errorMessage)
        assertFalse(state.isPlacingOrder)
        assertEquals(1, state.lines.size)
        assertNull(state.placedOrderId)
    }

    @Test
    fun `logged out user sees an empty cart`() = runTest {
        cart.setCart(userId, listOf(CartLine(latte, 1)))
        auth.logout()
        val viewModel = createViewModel()
        collect(viewModel)

        assertTrue(viewModel.uiState.value.lines.isEmpty())
    }
}
