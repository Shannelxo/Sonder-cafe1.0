package com.sondercafe.app.data.repository

import com.sondercafe.app.core.validation.InputSanitizer
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.data.local.dao.MenuDao
import com.sondercafe.app.data.mapper.toDomain
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val menuDao: MenuDao
) : MenuRepository {

    override fun searchMenu(query: String, category: MenuCategory?, availableOnly: Boolean): Flow<List<MenuItem>> {
        val cleanQuery = escapeLikeWildcards(InputSanitizer.singleLine(query, Validators.SEARCH_MAX_LENGTH))
        return menuDao.search(cleanQuery, category, availableOnly).map { items -> items.map { it.toDomain() } }
    }

    override fun observeMenuItem(id: Long): Flow<MenuItem?> =
        menuDao.observeById(id).map { it?.toDomain() }

    override suspend fun getAvailableItems(): List<MenuItem> =
        menuDao.getAvailable().map { it.toDomain() }

    // Without this, typing "%" or "_" would act as a wildcard and match everything.
    private fun escapeLikeWildcards(text: String): String =
        text.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
}
