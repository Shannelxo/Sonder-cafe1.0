package com.sondercafe.app.ui.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sondercafe.app.ui.components.MessageBanner
import com.sondercafe.app.ui.orders.StatusBadge
import com.sondercafe.app.ui.orders.formatOrderTime

/** Collected orders shown below the queue; older ones stay in the database but aren't listed. */
private const val RECENT_COLLECTED_LIMIT = 10

@Composable
fun AdminConsoleScreen(
    onBack: () -> Unit,
    onOrderClick: (Long) -> Unit,
    viewModel: AdminConsoleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AdminConsoleContent(
        state = state,
        onBack = onBack,
        onAdvance = viewModel::advance,
        onOrderClick = onOrderClick,
        onDismissError = viewModel::dismissError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleContent(
    state: AdminConsoleUiState,
    onBack: () -> Unit,
    onAdvance: (Order) -> Unit,
    onOrderClick: (Long) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin console") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.isAuthorised -> Column(Modifier.padding(padding).padding(16.dp)) {
                MessageBanner(message = "This area is for café staff only")
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

                item(key = "queue-header") { Header("Order queue (${state.activeOrders.size})") }
                if (state.activeOrders.isEmpty()) {
                    item(key = "queue-empty") {
                        Text("No orders waiting", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                items(state.activeOrders, key = { it.id }) { order ->
                    AdminOrderCard(
                        order = order,
                        isUpdating = order.id in state.updatingOrderIds,
                        onAdvance = { onAdvance(order) },
                        onOpen = { onOrderClick(order.id) }
                    )
                }

                if (state.collectedOrders.isNotEmpty()) {
                    item(key = "collected-header") { Header("Recently collected") }
                    items(state.collectedOrders.take(RECENT_COLLECTED_LIMIT), key = { it.id }) { order ->
                        AdminOrderCard(order = order, isUpdating = false, onAdvance = null, onOpen = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

@Composable
private fun AdminOrderCard(
    order: Order,
    isUpdating: Boolean,
    onAdvance: (() -> Unit)?,
    onOpen: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusBadge(order.status)
            }
            Text(
                text = order.lines.joinToString { "${it.quantity} × ${it.name}" },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${formatOrderTime(order.createdAt)} · ${MoneyFormatter.format(order.totalPence)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onOpen) { Text("Details") }
                val next = order.status.next()
                if (onAdvance != null && next != null) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        Button(onClick = onAdvance, enabled = !isUpdating) {
                            Text(if (isUpdating) "Updating…" else "Move to ${next.label}")
                        }
                    }
                }
            }
        }
    }
}
