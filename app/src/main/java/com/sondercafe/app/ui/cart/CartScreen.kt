package com.sondercafe.app.ui.cart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.ui.components.MessageBanner
import com.sondercafe.app.ui.components.QuantityStepper

@Composable
fun CartScreen(
    onOrderPlaced: (Long) -> Unit,
    onBrowseMenu: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.placedOrderId) {
        state.placedOrderId?.let { id ->
            viewModel.onOrderPlacedHandled()
            onOrderPlaced(id)
        }
    }

    // Order updates arrive as notifications, so ask for the permission (Android 13+) at the
    // moment it obviously makes sense: when the first order is placed. The order goes ahead
    // whatever the answer; the status is also visible in the app.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.placeOrder()
    }
    val onPlaceOrder = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.placeOrder()
        }
    }

    CartContent(
        state = state,
        onQuantityChange = viewModel::changeQuantity,
        onRemove = viewModel::removeItem,
        onPlaceOrder = onPlaceOrder,
        onDismissError = viewModel::dismissError,
        onBrowseMenu = onBrowseMenu
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartUiState,
    onQuantityChange: (menuItemId: Long, quantity: Int) -> Unit,
    onRemove: (menuItemId: Long) -> Unit,
    onPlaceOrder: () -> Unit,
    onDismissError: () -> Unit,
    onBrowseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Your cart") }) },
        bottomBar = {
            if (state.lines.isNotEmpty()) {
                CheckoutBar(state = state, onPlaceOrder = onPlaceOrder)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.lines.isEmpty() -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                state.errorMessage?.let { MessageBanner(message = it) }
                Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onBrowseMenu) { Text("Browse the menu") }
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                state.errorMessage?.let { message ->
                    item(key = "error") {
                        MessageBanner(message = message, actionLabel = "Dismiss", onAction = onDismissError)
                    }
                }
                if (state.hasUnavailableItems) {
                    item(key = "unavailable") {
                        MessageBanner(message = "Some items are no longer available. Remove them to place your order.")
                    }
                }
                items(state.lines, key = { it.menuItem.id }) { line ->
                    CartLineCard(
                        line = line,
                        enabled = !state.isPlacingOrder,
                        onQuantityChange = { onQuantityChange(line.menuItem.id, it) },
                        onRemove = { onRemove(line.menuItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    enabled: Boolean,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val item = line.menuItem
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${MoneyFormatter.format(item.pricePence)} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!item.isAvailable) {
                        Text("Unavailable", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
                IconButton(onClick = onRemove, enabled = enabled) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remove ${item.name}")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(
                    quantity = line.quantity,
                    onQuantityChange = onQuantityChange,
                    itemName = item.name,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = MoneyFormatter.format(line.lineTotalPence),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckoutBar(state: CartUiState, onPlaceOrder: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                val items = if (state.itemCount == 1) "1 item" else "${state.itemCount} items"
                Text("Total ($items)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(MoneyFormatter.format(state.totalPence), style = MaterialTheme.typography.titleLarge)
            }
            Button(
                onClick = onPlaceOrder,
                enabled = state.canPlaceOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (state.isPlacingOrder) "Placing order…" else "Place order")
            }
        }
    }
}
