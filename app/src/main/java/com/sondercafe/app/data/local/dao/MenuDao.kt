package com.sondercafe.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sondercafe.app.data.local.entity.MenuItemEntity
import com.sondercafe.app.domain.model.MenuCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {

    /**
     * All filters in one query. [query] must already have LIKE wildcards escaped with a
     * backslash, so a search for "50%" matches literally.
     */
    @Query(
        """
        SELECT * FROM menu_items
        WHERE (:query = '' OR name LIKE '%' || :query || '%' ESCAPE '\')
          AND (:category IS NULL OR category = :category)
          AND (:availableOnly = 0 OR is_available = 1)
        ORDER BY name
        """
    )
    fun search(query: String, category: MenuCategory?, availableOnly: Boolean): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    fun observeById(id: Long): Flow<MenuItemEntity?>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun findById(id: Long): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE is_available = 1")
    suspend fun getAvailable(): List<MenuItemEntity>

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MenuItemEntity>)
}
