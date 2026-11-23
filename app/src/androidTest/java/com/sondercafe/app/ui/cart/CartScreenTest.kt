package com.sondercafe.app.ui.cart

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.ui.theme.SonderCafeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val latte = menuItem(1, "Latte", 350)
    private val brownie = menuItem(2, "Brownie", 295, available = false)

    private fun setContent(
        state: CartUiState,
        onQuantityChange: (Long, Int) -> Unit = { _, _ -> },
        onRemove: (Long) -> Unit = {},
        onPlaceOrder: () -> Unit = {},
        onBrowseMenu: () -> Unit = {}
    ) {
        composeRule.setContent {
            SonderCafeTheme {
                CartContent(
                    state = state,
                    onQuantityChange = onQuantityChange,
                    onRemove = onRemove,
                    onPlaceOrder = onPlaceOrder,
                    onDismissError = {},
                    onBrowseMenu = onBrowseMenu
                )
            }
        }
    }

    @Test
    fun emptyCartOffersTheMenu() {
        var browsed = false
        setContent(CartUiState(isLoading = false), onBrowseMenu = { browsed = true })

        composeRule.onNodeWithText("Your cart is empty").assertIsDisplayed()
        composeRule.onNodeWithText("Browse the menu").performClick()
        assertTrue(browsed)
    }

    @Test
    fun showsLineAndOrderTotals() {
        setContent(CartUiState(lines = listOf(CartLine(latte, 2)), isLoading = false))

        composeRule.onNodeWithText("Latte").assertIsDisplayed()
        composeRule.onNodeWithText("Total (2 items)").assertIsDisplayed()
        // £7.00 appears as both the line total and the order total.
        composeRule.onAllNodesWithText("£7.00").assertCountEquals(2)
    }

    @Test
    fun increaseButtonReportsNewQuantity() {
        var change: Pair<Long, Int>? = null
        setContent(
            CartUiState(lines = listOf(CartLine(latte, 2)), isLoading = false),
            onQuantityChange = { id, qty -> change = id to qty }
        )

        composeRule.onNodeWithContentDescription("Increase quantity of Latte").performClick()

        assertEquals(1L to 3, change)
    }

    @Test
    fun removeButtonIsLabelledWithItemName() {
        var removed: Long? = null
        setContent(CartUiState(lines = listOf(CartLine(latte, 1)), isLoading = false), onRemove = { removed = it })

        composeRule.onNodeWithContentDescription("Remove Latte").performClick()

        assertEquals(1L, removed)
    }

    @Test
    fun unavailableItemDisablesCheckout() {
        setContent(CartUiState(lines = listOf(CartLine(latte, 1), CartLine(brownie, 1)), isLoading = false))

        composeRule.onNodeWithText("Some items are no longer available. Remove them to place your order.").assertIsDisplayed()
        composeRule.onNodeWithText("Place order").assertIsNotEnabled()
    }

    @Test
    fun placeOrderButtonSubmits() {
        var placed = false
        setContent(CartUiState(lines = listOf(CartLine(latte, 1)), isLoading = false), onPlaceOrder = { placed = true })

        composeRule.onNodeWithText("Place order").assertIsEnabled().performClick()

        assertTrue(placed)
    }

    private fun menuItem(id: Long, name: String, pricePence: Int, available: Boolean = true) = MenuItem(
        id = id,
        name = name,
        description = "$name description",
        category = MenuCategory.CAKES,
        pricePence = pricePence,
        isAvailable = available,
        servingTemperature = ServingTemperature.NEUTRAL
    )
}
