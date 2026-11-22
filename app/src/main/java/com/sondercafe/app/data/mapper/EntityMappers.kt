package com.sondercafe.app.data.mapper

import com.sondercafe.app.data.local.entity.FeedbackEntity
import com.sondercafe.app.data.local.entity.MenuItemEntity
import com.sondercafe.app.data.local.entity.OrderItemEntity
import com.sondercafe.app.data.local.entity.UserEntity
import com.sondercafe.app.data.local.relation.CartItemWithMenuItem
import com.sondercafe.app.data.local.relation.OrderWithItems
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.Feedback
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderLine
import com.sondercafe.app.domain.model.User

// Conversions between Room entities and domain models. Keeping them in one place means the
// database schema can change without touching the UI or the business logic.

fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName,
    role = role
)

fun MenuItemEntity.toDomain() = MenuItem(
    id = id,
    name = name,
    description = description,
    category = category,
    pricePence = pricePence,
    isAvailable = isAvailable,
    servingTemperature = servingTemperature
)

fun CartItemWithMenuItem.toDomain() = CartLine(
    menuItem = menuItem.toDomain(),
    quantity = cartItem.quantity
)

fun OrderItemEntity.toDomain() = OrderLine(
    menuItemId = menuItemId,
    name = nameSnapshot,
    unitPricePence = unitPricePence,
    quantity = quantity
)

fun OrderWithItems.toDomain() = Order(
    id = order.id,
    userId = order.userId,
    status = order.status,
    lines = items.sortedBy { it.id }.map { it.toDomain() },
    totalPence = order.totalPence,
    createdAt = order.createdAt,
    updatedAt = order.updatedAt
)

fun FeedbackEntity.toDomain() = Feedback(
    id = id,
    userId = userId,
    orderId = orderId,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)

/** The order id is filled in by OrderDao.placeOrder once the order row exists. */
fun CartLine.toOrderItemEntity() = OrderItemEntity(
    orderId = 0,
    menuItemId = menuItem.id,
    nameSnapshot = menuItem.name,
    unitPricePence = menuItem.pricePence,
    quantity = quantity
)
