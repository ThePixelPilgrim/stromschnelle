package de.nereide.stromschnelle.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import de.nereide.stromschnelle.StromschnelleApp
import kotlinx.coroutines.flow.first

/** Toggles a todo's completion state from the widget, then refreshes the widget UI. */
class ToggleCompleteAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoId = parameters[todoIdKey]
        // Instrumentation: confirms the callback fires and with which id.
        // Inspect with: adb logcat -s StromschnelleWidget
        Log.i(TAG, "onAction fired, todoId=$todoId")
        if (todoId == null) {
            Log.w(TAG, "onAction received no todoId parameter — aborting")
            return
        }
        val repository = (context.applicationContext as StromschnelleApp).container.todoRepository

        val current = repository.todo(todoId).first()
        if (current == null) {
            Log.w(TAG, "onAction: todo $todoId not found")
        } else {
            if (current.completedAt == null) {
                repository.complete(todoId)
                Log.i(TAG, "onAction: completed $todoId")
            } else {
                repository.uncomplete(todoId)
                Log.i(TAG, "onAction: uncompleted $todoId")
            }
        }

        StromschnelleWidget().updateAll(context)
    }

    companion object {
        private const val TAG = "StromschnelleWidget"
        val todoIdKey = ActionParameters.Key<Long>("todoId")
    }
}
