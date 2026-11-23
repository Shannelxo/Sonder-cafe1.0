package com.sondercafe.app.ui.menu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.Recommendation
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.domain.model.WeatherSnapshot
import com.sondercafe.app.ui.theme.SonderCafeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val latte = menuItem(1, "Latte", 350)
    private val icedLatte = menuItem(2, "Iced latte", 380, available = false)

    private fun setContent(
        state: MenuUiState,
        onQueryChange: (String) -> Unit = {},
        onCategorySelected: (MenuCategory?) -> Unit = {},
        onItemClick: (Long) -> Unit = {}
    ) {
        composeRule.setContent {
            SonderCafeTheme {
                MenuContent(
                    state = state,
                    onQueryChange = onQueryChange,
                    onCategorySelected = onCategorySelected,
                    onAvailableOnlyChange = {},
                    onItemClick = onItemClick,
                    onRetryRecommendations = {}
                )
            }
        }
    }

    @Test
    fun showsMenuItemsWithPricesAndAvailability() {
        setContent(MenuUiState(items = listOf(latte, icedLatte), isLoading = false))

        composeRule.onNodeWithText("Latte").assertIsDisplayed()
        composeRule.onNodeWithText("£3.50").assertIsDisplayed()
        composeRule.onNodeWithText("Unavailable").assertIsDisplayed()
    }

    @Test
    fun showsRecommendationReasonAndWeather() {
        val weather = WeatherSnapshot(temperatureCelsius = 8.4, weatherCode = 61, fetchedAt = Instant.now())
        setContent(
            MenuUiState(
                isLoading = false,
                recommendations = RecommendationsUiState(
                    isLoading = false,
                    items = listOf(Recommendation(latte, 0.9, "Something warm for a 8°C rainy day")),
                    weather = weather
                )
            )
        )

        composeRule.onNodeWithText("Something warm for a 8°C rainy day").assertIsDisplayed()
        composeRule.onNodeWithText("It's 8°C and rainy at the café").assertIsDisplayed()
    }

    @Test
    fun explainsWhenWeatherIsUnavailable() {
        setContent(
            MenuUiState(
                isLoading = false,
                recommendations = RecommendationsUiState(
                    isLoading = false,
                    items = listOf(Recommendation(latte, 0.5, "Worth a try")),
                    weather = null
                )
            )
        )

        composeRule
            .onNodeWithText("Weather isn't available right now, so these are based on orders and ratings only.")
            .assertIsDisplayed()
    }

    @Test
    fun showsEmptyStateForSearchWithNoResults() {
        setContent(MenuUiState(query = "matcha", isLoading = false))

        composeRule.onNodeWithText("No items match \"matcha\"").assertIsDisplayed()
    }

    @Test
    fun tappingCategoryChipReportsIt() {
        var selected: MenuCategory? = null
        setContent(MenuUiState(isLoading = false), onCategorySelected = { selected = it })

        composeRule.onNodeWithText("Tea").performClick()

        assertEquals(MenuCategory.TEA, selected)
    }

    @Test
    fun typingInSearchReportsQuery() {
        var query = ""
        setContent(MenuUiState(isLoading = false), onQueryChange = { query = it })

        composeRule.onNodeWithText("Search the menu").performTextInput("tea")

        assertEquals("tea", query)
    }

    @Test
    fun tappingItemOpensIt() {
        var opened: Long? = null
        setContent(MenuUiState(items = listOf(latte), isLoading = false, query = "lat"), onItemClick = { opened = it })

        composeRule.onNodeWithText("Latte").performClick()

        assertEquals(1L, opened)
    }

    private fun menuItem(id: Long, name: String, pricePence: Int, available: Boolean = true) = MenuItem(
        id = id,
        name = name,
        description = "$name description",
        category = MenuCategory.COFFEE,
        pricePence = pricePence,
        isAvailable = available,
        servingTemperature = ServingTemperature.HOT
    )
}
