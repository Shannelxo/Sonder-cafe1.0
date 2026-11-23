package com.sondercafe.app.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sondercafe.app.core.util.MoneyFormatter
import com.sondercafe.app.domain.model.Recommendation
import com.sondercafe.app.ui.components.MessageBanner
import com.sondercafe.app.ui.components.MessageType
import kotlin.math.roundToInt

/**
 * "Picked for you" strip at the top of the menu. Each card shows why it was suggested, so the
 * recommendations don't feel random.
 */
@Composable
fun RecommendationRow(
    state: RecommendationsUiState,
    onItemClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Picked for you",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp)
            )

            state.errorMessage != null -> MessageBanner(
                message = state.errorMessage,
                actionLabel = "Retry",
                onAction = onRetry,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            state.items.isEmpty() -> Text(
                text = "Nothing to suggest yet. Have a look through the menu below.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            else -> {
                val weather = state.weather
                if (weather != null) {
                    Text(
                        text = "It's ${weather.temperatureCelsius.roundToInt()}°C and ${weather.conditions} at the café",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    MessageBanner(
                        message = "Weather isn't available right now, so these are based on orders and ratings only.",
                        type = MessageType.INFO,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.menuItem.id }) { recommendation ->
                        RecommendationCard(recommendation, onClick = { onItemClick(recommendation.menuItem.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.width(200.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(12.dp)) {
            Text(
                text = recommendation.menuItem.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = MoneyFormatter.format(recommendation.menuItem.pricePence),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
