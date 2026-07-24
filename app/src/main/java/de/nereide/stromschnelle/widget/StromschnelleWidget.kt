package de.nereide.stromschnelle.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextDecoration
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.nereide.stromschnelle.StromschnelleApp
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoIcon
import kotlinx.coroutines.flow.first

/** Home-screen widget listing visible todos with inline completion toggles. */
class StromschnelleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as StromschnelleApp).container.todoRepository
        val todos = repository.visibleTodos.first()

        provideContent {
            if (todos.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().appWidgetBackground().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "No todos", style = TextStyle(fontSize = 14.sp))
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize().appWidgetBackground()) {
                    items(todos, itemId = { it.id }) { todo ->
                        TodoRow(todo)
                    }
                }
            }
        }
    }

    @Composable
    private fun TodoRow(todo: Todo) {
        val openDetailIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("stromschnelle://todo/${todo.id}")
        )
        val icon = TodoIcon.fromKey(todo.iconKey)
        val isCompleted = todo.completedAt != null

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glance cannot render a Compose ImageVector directly; the icon key is
            // surfaced as a short glyph so the row still communicates the category.
            Text(
                text = icon.key.take(1).uppercase(),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = todo.title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(openDetailIntent))
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = if (isCompleted) "☑" else "☐",
                style = TextStyle(fontSize = 18.sp),
                modifier = GlanceModifier.clickable(
                    actionRunCallback<ToggleCompleteAction>(
                        actionParametersOf(ToggleCompleteAction.todoIdKey to todo.id)
                    )
                )
            )
        }
    }
}
