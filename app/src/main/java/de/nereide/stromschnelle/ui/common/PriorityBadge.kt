package de.nereide.stromschnelle.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.nereide.stromschnelle.ui.theme.PriorityPalette

/** Which of the two scales a badge shows. */
enum class PriorityDimension { IMPORTANCE, EFFORT }

/**
 * One priority chip: a 24 dp coloured square carrying its digit.
 *
 * The digit is always rendered — colour never carries the meaning alone, which
 * keeps the badge readable in sunlight and with any colour vision deficiency.
 *
 * When [onClick] is non-null the visible chip stays 24 dp but the touch target
 * grows to the Material minimum of 48 dp via [minimumInteractiveComponentSize].
 */
@Composable
fun PriorityBadge(
    value: Int,
    dimension: PriorityDimension,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    dimmed: Boolean = false
) {
    val dark = isSystemInDarkTheme()
    val colors = when (dimension) {
        PriorityDimension.IMPORTANCE -> PriorityPalette.importance(value, dark)
        PriorityDimension.EFFORT -> PriorityPalette.effort(value, dark)
    }
    val label = when (dimension) {
        PriorityDimension.IMPORTANCE -> "Importance $value"
        PriorityDimension.EFFORT -> "Effort $value"
    }

    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .alpha(if (dimmed) 0.4f else 1f)
                .clip(RoundedCornerShape(7.dp))
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                color = colors.foreground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
