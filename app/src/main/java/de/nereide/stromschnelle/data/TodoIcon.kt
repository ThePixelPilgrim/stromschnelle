package de.nereide.stromschnelle.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Selectable icons for a [Todo]. The [key] is what gets persisted; [imageVector]
 * is resolved lazily at render time so the enum stays serialization-friendly.
 */
enum class TodoIcon(val key: String, val imageVector: ImageVector) {
    STAR("star", Icons.Default.Star),
    FLAG("flag", Icons.Default.Flag),
    CHECKLIST("checklist", Icons.Default.Checklist),
    HOME("home", Icons.Default.Home),
    WORK("work", Icons.Default.Work),
    GROCERIES("groceries", Icons.Default.LocalGroceryStore),
    PAYMENTS("payments", Icons.Default.Payments),
    FAVORITE("favorite", Icons.Default.Favorite),
    FITNESS("fitness", Icons.Default.FitnessCenter),
    IDEA("idea", Icons.Default.Lightbulb),
    CALENDAR("calendar", Icons.Default.CalendarMonth),
    URGENT("urgent", Icons.Default.Whatshot);

    companion object {
        /** Resolves a persisted key back to an icon, falling back to the first entry. */
        fun fromKey(key: String): TodoIcon =
            entries.firstOrNull { it.key == key } ?: entries.first()
    }
}
