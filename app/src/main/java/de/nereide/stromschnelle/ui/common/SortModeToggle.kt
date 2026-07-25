package de.nereide.stromschnelle.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import de.nereide.stromschnelle.data.SortMode
import de.nereide.stromschnelle.ui.theme.PriorityPalette

/**
 * Two lettered chips — `W` for importance (Wichtigkeit), `A` for effort
 * (Aufwand) — with the dominant dimension full size and the other small and
 * dimmed. One tap swaps them.
 *
 * Reuses the badge colour language instead of a generic sort icon, so the
 * button shows the current *state*, not just the available action.
 */
@Composable
fun SortModeToggle(
    mode: SortMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val importanceColors = PriorityPalette.importance(3, dark)
    val effortColors = PriorityPalette.effort(1, dark)
    val importanceLeads = mode == SortMode.IMPORTANCE_FIRST
    val label = if (importanceLeads) {
        "Sorted by importance, tap to sort by effort"
    } else {
        "Sorted by effort, tap to sort by importance"
    }

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 6.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (importanceLeads) {
            ToggleChip("W", importanceColors.background, importanceColors.foreground, 20.dp, false)
            ToggleChip("A", effortColors.background, effortColors.foreground, 14.dp, true)
        } else {
            ToggleChip("A", effortColors.background, effortColors.foreground, 20.dp, false)
            ToggleChip("W", importanceColors.background, importanceColors.foreground, 14.dp, true)
        }
    }
}

@Composable
private fun ToggleChip(
    letter: String,
    background: Color,
    foreground: Color,
    size: Dp,
    subordinate: Boolean
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 1.5.dp)
            .size(size)
            .alpha(if (subordinate) 0.5f else 1f)
            .clip(RoundedCornerShape(if (subordinate) 4.dp else 6.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = foreground,
            fontSize = if (subordinate) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
