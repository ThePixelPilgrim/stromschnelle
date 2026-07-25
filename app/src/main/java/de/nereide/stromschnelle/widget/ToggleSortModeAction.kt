package de.nereide.stromschnelle.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import de.nereide.stromschnelle.StromschnelleApp
import de.nereide.stromschnelle.data.SortMode
import kotlinx.coroutines.flow.first

/** Swaps the global sort mode from the widget header, then refreshes. */
class ToggleSortModeAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val settings = (context.applicationContext as StromschnelleApp).container.settingsRepository
        val next = when (settings.sortMode.first()) {
            SortMode.IMPORTANCE_FIRST -> SortMode.EFFORT_FIRST
            SortMode.EFFORT_FIRST -> SortMode.IMPORTANCE_FIRST
        }
        settings.setSortMode(next)
        Log.i("StromschnelleWidget", "sort mode toggled to $next")

        StromschnelleWidget().updateAll(context)
    }
}
