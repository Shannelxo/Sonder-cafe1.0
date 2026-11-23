package com.sondercafe.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.ui.components.MessageBanner
import com.sondercafe.app.ui.components.RatingBar

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    onLeaveFeedback: (Long) -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrderDetailContent(state = state, onBack = onBack, onLeaveFeedback = onLeaveFeedback)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailContent(
    state: OrderDetailUiState,
    onBack: () -> Unit,
    onLeaveFeedback: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.order?.let { "Order #${it.id}" } ?: "Order") },
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
        val order = state.order
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            order == null -> Column(Modifier.padding(padding).padding(16.dp)) {
                MessageBanner(message = state.errorMessage ?: "We couldn't find that order")
            }

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Placed ${formatOrderTime(order.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusTracker(current = order.status)
                OrderLines(order)

                val feedback = state.feedback
                when {
                    feedback != null -> Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Your feedback", style = MaterialTheme.typography.titleSmall)
                            RatingBar(rating = feedback.rating)
                            if (feedback.comment.isNotBlank()) Text(feedback.comment, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    state.canLeaveFeedback -> Button(
                        onClick = { onLeaveFeedback(order.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Rate this order")
                    }
                }
            }
        }
    }
}

/**
 * Pending → Preparing → Ready → Collected, with completed steps ticked. Announced as one
 * sentence, since four separate icons would mean nothing to a screen reader.
 */
@Composable
internal fun StatusTracker(current: OrderStatus, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Order status: ${current.label}"
        }
    ) {
        OrderStatus.entries.forEach { step ->
            val done = step.ordinal <= current.ordinal
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = step.label,
                    style = if (step == current) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrderLines(order: Order) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            order.lines.forEach { line ->
                Row(Modifier.semantics(mergeDescendants = true) {}) {
                    Text("${line.quantity} × ${line.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(MoneyFormatter.format(line.lineTotalPence), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
            Row(Modifier.semantics(mergeDescendants = true) {}) {
                Text("Total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(MoneyFormatter.format(order.totalPence), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
