package com.sondercafe.app.ui.menu

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.ui.components.MessageBanner

@Composable
fun MenuScreen(
    onItemClick: (Long) -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MenuContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onAvailableOnlyChange = viewModel::onAvailableOnlyChange,
        onItemClick = onItemClick,
        onRetryRecommendations = viewModel::loadRecommendations
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuContent(
    state: MenuUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (MenuCategory?) -> Unit,
    onAvailableOnlyChange: (Boolean) -> Unit,
    onItemClick: (Long) -> Unit,
    onRetryRecommendations: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Menu") }) },
        // The bottom bar's scaffold already handles the navigation bar inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(key = "search") {
                SearchField(query = state.query, onQueryChange = onQueryChange)
            }
            item(key = "filters") {
                Filters(
                    selected = state.category,
                    availableOnly = state.availableOnly,
                    onCategorySelected = onCategorySelected,
                    onAvailableOnlyChange = onAvailableOnlyChange
                )
            }

            // Suggestions only make sense on the full menu, not inside a search.
            if (!state.hasFilters) {
                item(key = "recommendations") {
                    RecommendationRow(
                        state = state.recommendations,
                        onItemClick = onItemClick,
                        onRetry = onRetryRecommendations
                    )
                }
            }

            if (state.errorMessage != null) {
                item(key = "error") {
                    MessageBanner(message = state.errorMessage, modifier = Modifier.padding(horizontal = 16.dp))
                }
            } else if (!state.isLoading && state.items.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = if (state.query.isNotBlank()) "No items match \"${state.query.trim()}\"" else "No items to show",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            items(state.items, key = { it.id }) { item ->
                MenuItemCard(item = item, onClick = { onItemClick(item.id) })
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search the menu") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun Filters(
    selected: MenuCategory?,
    availableOnly: Boolean,
    onCategorySelected: (MenuCategory?) -> Unit,
    onAvailableOnlyChange: (Boolean) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            FilterChip(selected = selected == null, onClick = { onCategorySelected(null) }, label = { Text("All") })
            MenuCategory.entries.forEach { category ->
                FilterChip(
                    selected = selected == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.label) }
                )
            }
        }
        // The whole row is one toggle, so the label is part of the touch target.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = availableOnly, onValueChange = onAvailableOnlyChange, role = Role.Switch)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text("Available now only", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = availableOnly, onCheckedChange = null)
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!item.isAvailable) {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(MoneyFormatter.format(item.pricePence), style = MaterialTheme.typography.titleSmall)
        }
    }
}
