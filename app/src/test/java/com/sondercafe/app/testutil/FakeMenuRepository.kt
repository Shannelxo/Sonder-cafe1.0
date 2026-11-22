package com.sondercafe.app.testutil

import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMenuRepository(items: List<MenuItem> = emptyList()) : MenuRepository {

    val items = MutableStateFlow(items)

    override fun searchMenu(query: String, category: MenuCategory?, availableOnly: Boolean): Flow<List<MenuItem>> =
        items.map { list ->
            list.filter { item ->
                (query.isBlank() || item.name.contains(query, ignoreCase = true)) &&
                    (category == null || item.category == category) &&
                    (!availableOnly || item.isAvailable)
            }
        }

    override fun observeMenuItem(id: Long): Flow<MenuItem?> = items.map { list -> list.find { it.id == id } }

    override suspend fun getAvailableItems(): List<MenuItem> = items.value.filter { it.isAvailable }
}

/** Builds a menu item for tests, with sensible defaults for anything the test doesn't care about. */
fun sampleMenuItem(
    id: Long,
    name: String = "Item $id",
    category: MenuCategory = MenuCategory.COFFEE,
    pricePence: Int = 300,
    available: Boolean = true,
    temperature: ServingTemperature = ServingTemperature.NEUTRAL
) = MenuItem(
    id = id,
    name = name,
    description = "$name description",
    category = category,
    pricePence = pricePence,
    isAvailable = available,
    servingTemperature = temperature
)
