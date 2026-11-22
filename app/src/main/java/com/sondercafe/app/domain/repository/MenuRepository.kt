package com.sondercafe.app.domain.repository

import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import kotlinx.coroutines.flow.Flow

interface MenuRepository {

    /**
     * Menu items matching all of the given filters. A blank [query] matches every name, and a
     * null [category] matches every category.
     */
    fun searchMenu(query: String, category: MenuCategory?, availableOnly: Boolean): Flow<List<MenuItem>>

    fun observeMenuItem(id: Long): Flow<MenuItem?>

    suspend fun getAvailableItems(): List<MenuItem>
}
