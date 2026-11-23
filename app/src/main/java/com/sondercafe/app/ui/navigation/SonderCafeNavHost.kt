package com.sondercafe.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sondercafe.app.ui.account.AccountScreen
import com.sondercafe.app.ui.admin.AdminConsoleScreen
import com.sondercafe.app.ui.auth.LoginScreen
import com.sondercafe.app.ui.auth.RegisterScreen
import com.sondercafe.app.ui.cart.CartScreen
import com.sondercafe.app.ui.feedback.FeedbackScreen
import com.sondercafe.app.ui.menu.MenuItemDetailScreen
import com.sondercafe.app.ui.menu.MenuScreen
import com.sondercafe.app.ui.orders.OrderConfirmationScreen
import com.sondercafe.app.ui.orders.OrderDetailScreen
import com.sondercafe.app.ui.orders.OrdersScreen

/**
 * The whole app's navigation. Login state drives it: logging out (or deleting the account)
 * from anywhere clears the back stack and returns to the login screen.
 *
 * [pendingOrderId] is set when the app is opened from an order notification.
 */
@Composable
fun SonderCafeNavHost(
    pendingOrderId: Long?,
    onPendingOrderHandled: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    val cartItemCount by sessionViewModel.cartItemCount.collectAsStateWithLifecycle()

    if (session == SessionState.Loading) {
        Surface(Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    val navController = rememberNavController()
    // Chosen once. Later login/logout changes are handled by navigating, not by swapping the graph.
    val startDestination = remember { if (session is SessionState.LoggedIn) Routes.MENU else Routes.LOGIN }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(session) {
        val onAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER
        if (session == SessionState.LoggedOut && currentRoute != null && !onAuthScreen) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    LaunchedEffect(pendingOrderId, session) {
        if (pendingOrderId != null && session is SessionState.LoggedIn) {
            navController.navigate(Routes.orderDetail(pendingOrderId)) { launchSingleTop = true }
            onPendingOrderHandled()
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                BottomBar(
                    currentRoute = currentRoute,
                    cartItemCount = cartItemCount,
                    onNavigate = { navController.navigateToTopLevel(it) }
                )
            }
        }
    ) { innerPadding ->
        // Only the bottom padding is applied here. Screens with a top app bar handle the
        // status bar themselves; the ones without one add statusBarsPadding below.
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            composable(Routes.LOGIN) {
                Box(Modifier.statusBarsPadding()) {
                    LoginScreen(
                        onLoggedIn = { navController.enterApp() },
                        onCreateAccount = { navController.navigate(Routes.REGISTER) }
                    )
                }
            }
            composable(Routes.REGISTER) {
                Box(Modifier.statusBarsPadding()) {
                    RegisterScreen(
                        onRegistered = { navController.enterApp() },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.MENU) {
                MenuScreen(onItemClick = { navController.navigate(Routes.menuItem(it)) })
            }
            composable(
                Routes.MENU_ITEM,
                arguments = listOf(navArgument(Routes.ARG_ITEM_ID) { type = NavType.LongType })
            ) {
                MenuItemDetailScreen(
                    onBack = { navController.popBackStack() },
                    onViewCart = { navController.navigateToTopLevel(Routes.CART) }
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onOrderPlaced = { navController.navigate(Routes.orderConfirmation(it)) },
                    onBrowseMenu = { navController.navigateToTopLevel(Routes.MENU) }
                )
            }

            composable(
                Routes.ORDER_CONFIRMATION,
                arguments = listOf(navArgument(Routes.ARG_ORDER_ID) { type = NavType.LongType })
            ) {
                Box(Modifier.statusBarsPadding()) {
                    OrderConfirmationScreen(
                        onTrackOrder = { orderId ->
                            navController.navigate(Routes.orderDetail(orderId)) {
                                popUpTo(Routes.ORDER_CONFIRMATION) { inclusive = true }
                            }
                        },
                        onBackToMenu = { navController.navigateToTopLevel(Routes.MENU) }
                    )
                }
            }
            composable(Routes.ORDERS) {
                OrdersScreen(onOrderClick = { navController.navigate(Routes.orderDetail(it)) })
            }
            composable(
                Routes.ORDER_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_ORDER_ID) { type = NavType.LongType })
            ) {
                OrderDetailScreen(
                    // navigateUp never pops the last screen, unlike popBackStack.
                    onBack = { navController.navigateUp() },
                    onLeaveFeedback = { navController.navigate(Routes.feedback(it)) }
                )
            }
            composable(
                Routes.FEEDBACK,
                arguments = listOf(navArgument(Routes.ARG_ORDER_ID) { type = NavType.LongType })
            ) {
                FeedbackScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitted = { navController.popBackStack() }
                )
            }

            composable(Routes.ACCOUNT) {
                AccountScreen(onOpenAdminConsole = { navController.navigate(Routes.ADMIN) })
            }
            composable(Routes.ADMIN) {
                val isAdmin = (session as? SessionState.LoggedIn)?.user?.isAdmin == true
                if (isAdmin) {
                    AdminConsoleScreen(
                        onBack = { navController.popBackStack() },
                        onOrderClick = { navController.navigate(Routes.orderDetail(it)) }
                    )
                } else {
                    // First guard. AdminConsoleViewModel checks the role again on its own.
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                }
            }
        }
    }
}

/** After login or registration: go to the menu and drop the auth screens from the back stack. */
private fun NavHostController.enterApp() {
    navigate(Routes.MENU) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/**
 * Bottom bar navigation: one copy of each tab, with each tab's own back stack kept while
 * switching. The menu is always the root once logged in, so it's the pop-up target.
 */
private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(Routes.MENU) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
