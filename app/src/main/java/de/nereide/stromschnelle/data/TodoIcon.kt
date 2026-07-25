package de.nereide.stromschnelle.data

import androidx.annotation.DrawableRes
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
import de.nereide.stromschnelle.R

/**
 * Selectable icons for a [Todo]. The [key] is what gets persisted.
 *
 * Each icon exists twice: as a Compose [ImageVector] for the app, and as a
 * vector drawable [drawableRes] for the widget. Glance cannot render an
 * `ImageVector`, but it renders drawables via `ImageProvider(resId)`.
 */
enum class TodoIcon(
    val key: String,
    val imageVector: ImageVector,
    @DrawableRes val drawableRes: Int
) {
    STAR("star", Icons.Default.Star, R.drawable.ic_todo_star),
    FLAG("flag", Icons.Default.Flag, R.drawable.ic_todo_flag),
    CHECKLIST("checklist", Icons.Default.Checklist, R.drawable.ic_todo_checklist),
    HOME("home", Icons.Default.Home, R.drawable.ic_todo_home),
    WORK("work", Icons.Default.Work, R.drawable.ic_todo_work),
    GROCERIES("groceries", Icons.Default.LocalGroceryStore, R.drawable.ic_todo_groceries),
    PAYMENTS("payments", Icons.Default.Payments, R.drawable.ic_todo_payments),
    FAVORITE("favorite", Icons.Default.Favorite, R.drawable.ic_todo_favorite),
    FITNESS("fitness", Icons.Default.FitnessCenter, R.drawable.ic_todo_fitness),
    IDEA("idea", Icons.Default.Lightbulb, R.drawable.ic_todo_idea),
    CALENDAR("calendar", Icons.Default.CalendarMonth, R.drawable.ic_todo_calendar),
    URGENT("urgent", Icons.Default.Whatshot, R.drawable.ic_todo_urgent);

    companion object {
        /** Resolves a persisted key back to an icon, falling back to the first entry. */
        fun fromKey(key: String): TodoIcon =
            entries.firstOrNull { it.key == key } ?: entries.first()
    }
}
