package de.nereide.stromschnelle.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import de.nereide.stromschnelle.StromschnelleApp
import kotlinx.coroutines.flow.first

/** Toggles a todo's completion state from the widget, then refreshes the widget UI. */
class ToggleCompleteAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoId = parameters[todoIdKey] ?: return
        val repository = (context.applicationContext as StromschnelleApp).container.todoRepository

        val current = repository.todo(todoId).first()
        if (current != null) {
            if (current.completedAt == null) {
                repository.complete(todoId)
            } else {
                repository.uncomplete(todoId)
            }
        }

        StromschnelleWidget().updateAll(context)
    }

    companion object {
        val todoIdKey = ActionParameters.Key<Long>("todoId")
    }
}
