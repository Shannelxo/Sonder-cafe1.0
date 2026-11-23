package com.sondercafe.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.ui.components.MessageBanner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val orderTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK)

/** Order times in the phone's own time zone, e.g. "14 Nov 2026, 09:41". */
internal fun formatOrderTime(instant: Instant): String =
    orderTimeFormatter.format(instant.atZone(ZoneId.systemDefault()))

@Composable
fun OrdersScreen(
    onOrderClick: (Long) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrdersContent(state = state, onOrderClick = onOrderClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(
    state: OrdersUiState,
    onOrderClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Your orders") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> Column(Modifier.padding(padding).padding(16.dp)) {
                MessageBanner(message = state.errorMessage)
            }

            state.isEmpty -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You haven't placed any orders yet", style = MaterialTheme.typography.bodyLarge)
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.activeOrders.isNotEmpty()) {
                    item(key = "active-header") { SectionHeader("In progress") }
                    items(state.activeOrders, key = { it.id }) { order ->
                        OrderSummaryCard(order, onClick = { onOrderClick(order.id) })
                    }
                }
                if (state.pastOrders.isNotEmpty()) {
                    item(key = "past-header") { SectionHeader("Past orders") }
                    items(state.pastOrders, key = { it.id }) { order ->
                        OrderSummaryCard(order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

/** Plain status label (not a button), so the card stays a single tap target for TalkBack. */
@Composable
internal fun StatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val active = status.isActive
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

@Composable
internal fun OrderSummaryCard(order: Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusBadge(order.status)
            }
            val items = if (order.itemCount == 1) "1 item" else "${order.itemCount} items"
            Text(
                text = "$items · ${MoneyFormatter.format(order.totalPence)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatOrderTime(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
