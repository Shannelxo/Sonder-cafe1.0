package com.sondercafe.app.data.local.seed

import com.sondercafe.app.data.local.entity.MenuItemEntity
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuCategory.CAKES
import com.sondercafe.app.domain.model.MenuCategory.COFFEE
import com.sondercafe.app.domain.model.MenuCategory.COLD_DRINKS
import com.sondercafe.app.domain.model.MenuCategory.SNACKS
import com.sondercafe.app.domain.model.MenuCategory.TEA
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.domain.model.ServingTemperature.COLD
import com.sondercafe.app.domain.model.ServingTemperature.HOT
import com.sondercafe.app.domain.model.ServingTemperature.NEUTRAL

/**
 * The starting menu, inserted on first launch. A few items are marked unavailable so the
 * "sold out" behaviour can be seen straight away.
 */
object MenuSeedData {

    val items: List<MenuItemEntity> = listOf(
        item(1, "Espresso", "A short, intense single shot of our house blend.", COFFEE, 220, HOT),
        item(2, "Americano", "Espresso topped up with hot water for a longer, smoother cup.", COFFEE, 280, HOT),
        item(3, "Flat white", "Double espresso with a thin layer of velvety steamed milk.", COFFEE, 330, HOT),
        item(4, "Latte", "Espresso with plenty of steamed milk and a little foam.", COFFEE, 350, HOT),
        item(5, "Cappuccino", "Equal parts espresso, steamed milk and thick foam, dusted with cocoa.", COFFEE, 340, HOT),
        item(6, "Mocha", "Espresso, rich chocolate and steamed milk.", COFFEE, 380, HOT),

        item(7, "English breakfast tea", "A strong, malty black tea. Milk on the side.", TEA, 250, HOT),
        item(8, "Earl grey", "Black tea scented with bergamot.", TEA, 250, HOT),
        item(9, "Chai latte", "Spiced black tea with steamed milk and a hint of cinnamon.", TEA, 360, HOT),
        item(10, "Fresh mint tea", "Fresh mint leaves steeped in hot water.", TEA, 270, HOT),

        item(11, "Iced latte", "Espresso and cold milk over ice.", COLD_DRINKS, 380, COLD),
        item(12, "Iced americano", "Espresso over ice, topped with cold water.", COLD_DRINKS, 320, COLD),
        item(13, "Cold brew", "Coffee steeped cold for 18 hours. Smooth and low in acidity.", COLD_DRINKS, 360, COLD, available = false),
        item(14, "Fresh orange juice", "Squeezed to order.", COLD_DRINKS, 300, COLD),
        item(15, "Strawberry smoothie", "Strawberries, banana and apple juice, blended with ice.", COLD_DRINKS, 420, COLD),

        item(16, "Chocolate brownie", "Dense and fudgy, made with dark chocolate.", CAKES, 295, NEUTRAL),
        item(17, "Carrot cake", "Spiced sponge with walnuts and cream cheese frosting.", CAKES, 375, NEUTRAL),
        item(18, "Lemon drizzle", "Light lemon sponge soaked in a sharp lemon syrup.", CAKES, 350, NEUTRAL),
        item(19, "Blueberry muffin", "Baked fresh each morning.", CAKES, 280, NEUTRAL, available = false),
        item(20, "Victoria sponge", "Jam and buttercream between two vanilla sponges.", CAKES, 360, NEUTRAL),

        item(21, "Cheese toastie", "Mature cheddar on sourdough, toasted until golden.", SNACKS, 520, HOT),
        item(22, "Sausage roll", "Pork sausage meat in flaky pastry, served warm.", SNACKS, 380, HOT),
        item(23, "Butter croissant", "All-butter and flaky.", SNACKS, 260, NEUTRAL),
        item(24, "Bagel with cream cheese", "Toasted plain bagel with a generous spread of cream cheese.", SNACKS, 420, NEUTRAL),
        item(25, "Fruit pot", "Seasonal fruit, chopped fresh.", SNACKS, 300, COLD)
    )

    private fun item(
        id: Long,
        name: String,
        description: String,
        category: MenuCategory,
        pricePence: Int,
        temperature: ServingTemperature,
        available: Boolean = true
    ) = MenuItemEntity(
        id = id,
        name = name,
        description = description,
        category = category,
        pricePence = pricePence,
        isAvailable = available,
        servingTemperature = temperature
    )
}
