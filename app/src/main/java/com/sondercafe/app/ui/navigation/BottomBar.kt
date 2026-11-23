package com.sondercafe.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    TopLevelDestination(Routes.MENU, "Menu", Icons.Filled.RestaurantMenu),
    TopLevelDestination(Routes.CART, "Cart", Icons.Filled.ShoppingCart),
    TopLevelDestination(Routes.ORDERS, "Orders", Icons.Filled.Receipt),
    TopLevelDestination(Routes.ACCOUNT, "Account", Icons.Filled.Person)
)

/** Routes that show the bottom bar. Everything else is a detail screen with a back button. */
val topLevelRoutes: Set<String> = destinations.map { it.route }.toSet()

@Composable
fun BottomBar(
    currentRoute: String?,
    cartItemCount: Int,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                label = { Text(destination.label) },
                icon = {
                    if (destination.route == Routes.CART && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    // Read as "3 items in cart", not just "3".
                                    modifier = Modifier.clearAndSetSemantics {
                                        contentDescription = if (cartItemCount == 1) "1 item in cart" else "$cartItemCount items in cart"
                                    }
                                ) {
                                    Text(if (cartItemCount > 99) "99+" else cartItemCount.toString())
                                }
                            }
                        ) {
                            Icon(destination.icon, contentDescription = null)
                        }
                    } else {
                        // The label underneath already names the tab.
                        Icon(destination.icon, contentDescription = null)
                    }
                }
            )
        }
    }
}
