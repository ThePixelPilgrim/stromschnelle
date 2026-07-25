package de.nereide.stromschnelle.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import de.nereide.stromschnelle.StromschnelleApp
import de.nereide.stromschnelle.ui.common.PriorityDimension

/** Rotates one priority dimension of one todo from the widget, then refreshes. */
class CyclePriorityAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoId = parameters[todoIdKey]
        val dimensionName = parameters[dimensionKey]
        Log.i(TAG, "cycle fired, todoId=$todoId dimension=$dimensionName")

        if (todoId == null || dimensionName == null) {
            Log.w(TAG, "cycle received incomplete parameters — aborting")
            return
        }
        val dimension = PriorityDimension.entries.firstOrNull { it.name == dimensionName }
        if (dimension == null) {
            Log.w(TAG, "cycle received unknown dimension '$dimensionName' — aborting")
            return
        }

        val repository = (context.applicationContext as StromschnelleApp).container.todoRepository
        when (dimension) {
            PriorityDimension.IMPORTANCE -> repository.cycleImportance(todoId)
            PriorityDimension.EFFORT -> repository.cycleEffort(todoId)
        }

        StromschnelleWidget().updateAll(context)
    }

    companion object {
        private const val TAG = "StromschnelleWidget"
        val todoIdKey = ActionParameters.Key<Long>("todoId")
        val dimensionKey = ActionParameters.Key<String>("dimension")
    }
}
