package com.sondercafe.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sondercafe.app.core.validation.Validators

/**
 * Minus / quantity / plus control. The buttons switch off at the limits, so the user can't
 * pick a quantity the cart would reject. Button labels include the item name so screen reader
 * users know which item they're changing.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    itemName: String,
    modifier: Modifier = Modifier,
    min: Int = Validators.MIN_QUANTITY,
    max: Int = Validators.MAX_QUANTITY,
    enabled: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        FilledTonalIconButton(
            onClick = { onQuantityChange(quantity - 1) },
            enabled = enabled && quantity > min
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity of $itemName")
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 40.dp)
                .semantics { contentDescription = "Quantity of $itemName: $quantity" }
        )
        FilledTonalIconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = enabled && quantity < max
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase quantity of $itemName")
        }
    }
}
