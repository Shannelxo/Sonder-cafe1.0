package com.sondercafe.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sondercafe.app.core.validation.Validators

/**
 * Star rating. Pass [onRatingChange] to let the user pick a rating; leave it null to just show
 * one.
 *
 * When it's editable, the stars behave like a group of radio buttons, so screen readers read
 * "3 stars, selected" rather than five unlabelled icons. Read-only ratings are announced once
 * as a single phrase.
 */
@Composable
fun RatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    onRatingChange: ((Int) -> Unit)? = null,
    max: Int = Validators.MAX_RATING
) {
    val starColour = MaterialTheme.colorScheme.primary

    if (onRatingChange == null) {
        Row(
            modifier = modifier.clearAndSetSemantics {
                contentDescription = "Rated $rating out of $max"
            }
        ) {
            for (star in 1..max) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = starColour,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        return
    }

    Row(modifier = modifier.selectableGroup()) {
        for (star in 1..max) {
            val label = if (star == 1) "1 star" else "$star stars"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .selectable(
                        selected = star == rating,
                        onClick = { onRatingChange(star) },
                        role = Role.RadioButton
                    )
                    .semantics { contentDescription = label }
            ) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = starColour,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
