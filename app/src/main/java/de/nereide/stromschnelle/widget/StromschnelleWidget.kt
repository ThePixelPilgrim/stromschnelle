package de.nereide.stromschnelle.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import de.nereide.stromschnelle.StromschnelleApp
import de.nereide.stromschnelle.data.SortMode
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoIcon
import de.nereide.stromschnelle.ui.common.PriorityDimension
import de.nereide.stromschnelle.ui.theme.PriorityPalette
import kotlinx.coroutines.flow.first

/** Home-screen widget: header plus the priority-sorted list with inline controls. */
class StromschnelleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as StromschnelleApp).container
        val repository = container.todoRepository
        val settings = container.settingsRepository

        // Initial one-shot reads so the first frame renders synchronously with data.
        val initialTodos = repository.visibleTodosNow()
        val initialTitle = settings.widgetTitle.first()
        val initialMode = settings.sortMode.first()

        provideContent {
            // IMPORTANT: `provideGlance` is only invoked once per widget session;
            // `updateAll` merely recomposes the composition below. Data must
            // therefore be observed INSIDE the composition — the Room- and
            // DataStore-backed Flows emit on every change and drive recomposition,
            // so the widget repaints after any mutation (app-side or widget-side).
            val todos by repository.visibleTodos.collectAsState(initial = initialTodos)
            val title by settings.widgetTitle.collectAsState(initial = initialTitle)
            val sortMode by settings.sortMode.collectAsState(initial = initialMode)

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(GlanceTheme.colors.widgetBackground)
                        .cornerRadius(16.dp)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        HeaderRow(title = title, sortMode = sortMode)
                        if (todos.isEmpty()) {
                            Box(
                                modifier = GlanceModifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No todos",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        } else {
                            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                                items(todos, itemId = { it.id }) { todo -> TodoRow(todo) }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Title plus sort-mode toggle. A blank title renders no text at all — that is
     * how the header's text space is reclaimed without losing the toggle.
     */
    @Composable
    private fun HeaderRow(title: String, sortMode: SortMode) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 10.dp, end = 4.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            SortModeChips(sortMode)
        }
    }

    @Composable
    private fun SortModeChips(sortMode: SortMode) {
        val importanceLeads = sortMode == SortMode.IMPORTANCE_FIRST
        Row(
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(actionRunCallback<ToggleSortModeAction>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (importanceLeads) {
                SortChip("W", PriorityDimension.IMPORTANCE, 18.dp, 10.sp)
                SortChip("A", PriorityDimension.EFFORT, 13.dp, 8.sp)
            } else {
                SortChip("A", PriorityDimension.EFFORT, 18.dp, 10.sp)
                SortChip("W", PriorityDimension.IMPORTANCE, 13.dp, 8.sp)
            }
        }
    }

    @Composable
    private fun SortChip(
        letter: String,
        dimension: PriorityDimension,
        size: androidx.compose.ui.unit.Dp,
        fontSize: androidx.compose.ui.unit.TextUnit
    ) {
        // Anchor colours: importance uses its 3, effort its 1 — the two ends of
        // the scale that read most clearly at chip size.
        val light = when (dimension) {
            PriorityDimension.IMPORTANCE -> PriorityPalette.importance(3, dark = false)
            PriorityDimension.EFFORT -> PriorityPalette.effort(1, dark = false)
        }
        val dark = when (dimension) {
            PriorityDimension.IMPORTANCE -> PriorityPalette.importance(3, dark = true)
            PriorityDimension.EFFORT -> PriorityPalette.effort(1, dark = true)
        }
        Box(
            modifier = GlanceModifier
                .size(size)
                .cornerRadius(5.dp)
                .background(ColorProvider(day = light.background, night = dark.background)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                style = TextStyle(
                    color = ColorProvider(day = light.foreground, night = dark.foreground),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    @Composable
    private fun TodoRow(todo: Todo) {
        val openDetailIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("stromschnelle://todo/${todo.id}")
        )
        val isCompleted = todo.completedAt != null
        val titleColor = if (isCompleted) {
            GlanceTheme.colors.onSurfaceVariant
        } else {
            GlanceTheme.colors.onSurface
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WidgetPriorityBadge(
                value = todo.importance,
                dimension = PriorityDimension.IMPORTANCE,
                onClick = actionRunCallback<CyclePriorityAction>(
                    actionParametersOf(
                        CyclePriorityAction.todoIdKey to todo.id,
                        CyclePriorityAction.dimensionKey to PriorityDimension.IMPORTANCE.name
                    )
                )
            )
            WidgetPriorityBadge(
                value = todo.effort,
                dimension = PriorityDimension.EFFORT,
                onClick = actionRunCallback<CyclePriorityAction>(
                    actionParametersOf(
                        CyclePriorityAction.todoIdKey to todo.id,
                        CyclePriorityAction.dimensionKey to PriorityDimension.EFFORT.name
                    )
                )
            )
            // No maxLines: the title wraps rather than truncating.
            Text(
                text = todo.title,
                style = TextStyle(
                    color = titleColor,
                    fontSize = 14.sp,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity(openDetailIntent))
            )
            // Inert buffer between the title and the checkbox. The drawables ship
            // untinted because a theme attribute would not resolve in RemoteViews,
            // so the colour is applied here instead.
            Image(
                provider = ImageProvider(TodoIcon.fromKey(todo.iconKey).drawableRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            CheckBox(
                checked = isCompleted,
                onCheckedChange = actionRunCallback<ToggleCompleteAction>(
                    actionParametersOf(ToggleCompleteAction.todoIdKey to todo.id)
                )
            )
        }
    }
}
