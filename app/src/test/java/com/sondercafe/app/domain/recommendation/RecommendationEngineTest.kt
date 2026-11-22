package com.sondercafe.app.domain.recommendation

import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.domain.model.WeatherSnapshot
import com.sondercafe.app.testutil.sampleMenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RecommendationEngineTest {

    private val engine = RecommendationEngine()

    private val latte = sampleMenuItem(id = 1, name = "Latte", category = MenuCategory.COFFEE, temperature = ServingTemperature.HOT)
    private val icedLatte = sampleMenuItem(id = 2, name = "Iced latte", category = MenuCategory.COLD_DRINKS, temperature = ServingTemperature.COLD)
    private val brownie = sampleMenuItem(id = 3, name = "Brownie", category = MenuCategory.CAKES, temperature = ServingTemperature.NEUTRAL)
    private val soldOut = sampleMenuItem(id = 4, name = "Sold out muffin", category = MenuCategory.CAKES, available = false)

    private val menu = listOf(latte, icedLatte, brownie, soldOut)

    private fun weather(celsius: Double, code: Int = 0) = WeatherSnapshot(celsius, code, Instant.EPOCH)

    @Test
    fun `never recommends unavailable items`() {
        val result = engine.recommend(menu, mapOf(4L to 10), mapOf(4L to 50), emptyMap(), null)
        assertFalse(result.any { it.menuItem.id == soldOut.id })
    }

    @Test
    fun `personal history ranks a customer's usual order first`() {
        val result = engine.recommend(menu, personalCounts = mapOf(3L to 5), popularityCounts = emptyMap(), averageRatings = emptyMap(), weather = null)
        assertEquals(brownie, result.first().menuItem)
        assertEquals("One of your favourites", result.first().reason)
    }

    @Test
    fun `hot drinks rank above cold drinks on a cold rainy day`() {
        val result = engine.recommend(menu, emptyMap(), emptyMap(), emptyMap(), weather(celsius = 6.0, code = 61))
        val ids = result.map { it.menuItem.id }
        assertTrue(ids.indexOf(latte.id) < ids.indexOf(icedLatte.id))
        assertEquals("Something warm for a 6°C rainy day", result.first().reason)
    }

    @Test
    fun `cold drinks rank above hot drinks on a warm dry day`() {
        val result = engine.recommend(menu, emptyMap(), emptyMap(), emptyMap(), weather(celsius = 25.0))
        assertEquals(icedLatte, result.first().menuItem)
        assertEquals("Cool down: it's 25°C outside", result.first().reason)
    }

    @Test
    fun `still recommends when weather is unavailable`() {
        val result = engine.recommend(menu, emptyMap(), mapOf(2L to 8, 1L to 2), emptyMap(), weather = null)
        assertEquals(3, result.size)
        assertEquals(icedLatte, result.first().menuItem)
        assertEquals("Popular with other customers", result.first().reason)
    }

    @Test
    fun `strong history outweighs the weather`() {
        // A regular iced latte drinker should still see it first on a cold day.
        val result = engine.recommend(menu, mapOf(2L to 6), emptyMap(), emptyMap(), weather(celsius = 5.0))
        assertEquals(icedLatte, result.first().menuItem)
    }

    @Test
    fun `highly rated items explain their rating`() {
        val result = engine.recommend(listOf(brownie), emptyMap(), emptyMap(), mapOf(3L to 4.6), weather = null)
        assertEquals("Rated 4.6 out of 5", result.single().reason)
    }

    @Test
    fun `new customer with no data still gets a stable list`() {
        val first = engine.recommend(menu, emptyMap(), emptyMap(), emptyMap(), null)
        val second = engine.recommend(menu, emptyMap(), emptyMap(), emptyMap(), null)
        assertEquals(first, second)
        assertTrue(first.all { it.reason == "Worth a try" })
    }

    @Test
    fun `respects the limit`() {
        val result = engine.recommend(menu, emptyMap(), emptyMap(), emptyMap(), null, limit = 2)
        assertEquals(2, result.size)
    }

    @Test
    fun `weather fit boundaries`() {
        assertEquals(1.0, engine.weatherFit(ServingTemperature.HOT, weather(12.0)), 0.0)
        assertEquals(0.5, engine.weatherFit(ServingTemperature.HOT, weather(15.0)), 0.0)
        assertEquals(1.0, engine.weatherFit(ServingTemperature.COLD, weather(20.0)), 0.0)
        assertEquals(0.6, engine.weatherFit(ServingTemperature.COLD, weather(22.0, code = 80)), 0.0)
        assertEquals(0.5, engine.weatherFit(ServingTemperature.NEUTRAL, weather(30.0)), 0.0)
    }
}
