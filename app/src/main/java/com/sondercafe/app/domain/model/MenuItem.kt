package com.sondercafe.app.domain.model

enum class MenuCategory(val label: String) {
    COFFEE("Coffee"),
    TEA("Tea"),
    COLD_DRINKS("Cold drinks"),
    CAKES("Cakes"),
    SNACKS("Snacks")
}

/** Used by the recommendation engine to match items to the weather. */
enum class ServingTemperature { HOT, COLD, NEUTRAL }

data class MenuItem(
    val id: Long,
    val name: String,
    val description: String,
    val category: MenuCategory,
    val pricePence: Int,
    val isAvailable: Boolean,
    val servingTemperature: ServingTemperature
)
