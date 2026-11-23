package com.sondercafe.app.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.ui.components.MessageBanner
import com.sondercafe.app.ui.components.MessageType
import com.sondercafe.app.ui.components.QuantityStepper

@Composable
fun MenuItemDetailScreen(
    onBack: () -> Unit,
    onViewCart: () -> Unit,
    viewModel: MenuItemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MenuItemDetailContent(
        state = state,
        onBack = onBack,
        onQuantityChange = viewModel::onQuantityChange,
        onAddToCart = viewModel::addToCart,
        onViewCart = onViewCart
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailContent(
    state: MenuItemDetailUiState,
    onBack: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.name ?: "Menu item") },
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
        val item = state.item
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.notFound || item == null -> Column(Modifier.padding(padding).padding(16.dp)) {
                MessageBanner(message = state.errorMessage ?: "This item is no longer on the menu")
            }

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(item.category.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(MoneyFormatter.format(item.pricePence), style = MaterialTheme.typography.headlineSmall)
                Text(item.description, style = MaterialTheme.typography.bodyLarge)

                if (!item.isAvailable) {
                    MessageBanner(
                        message = "${item.name} is unavailable at the moment. Check back later.",
                        type = MessageType.INFO
                    )
                } else {
                    QuantityStepper(
                        quantity = state.quantity,
                        onQuantityChange = onQuantityChange,
                        itemName = item.name,
                        enabled = !state.isAdding
                    )
                    Button(
                        onClick = onAddToCart,
                        enabled = !state.isAdding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Add to cart · ${MoneyFormatter.format(item.pricePence * state.quantity)}")
                    }
                }

                state.addedMessage?.let { message ->
                    MessageBanner(message = message, type = MessageType.INFO, actionLabel = "View cart", onAction = onViewCart)
                }
                state.errorMessage?.let { message -> MessageBanner(message = message) }
            }
        }
    }
}
