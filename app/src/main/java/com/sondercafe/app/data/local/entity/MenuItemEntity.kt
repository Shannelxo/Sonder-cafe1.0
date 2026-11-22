package com.sondercafe.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.ServingTemperature

@Entity(
    tableName = "menu_items",
    indices = [Index(value = ["category"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: MenuCategory,
    @ColumnInfo(name = "price_pence") val pricePence: Int,
    @ColumnInfo(name = "is_available") val isAvailable: Boolean,
    @ColumnInfo(name = "serving_temperature") val servingTemperature: ServingTemperature
)
