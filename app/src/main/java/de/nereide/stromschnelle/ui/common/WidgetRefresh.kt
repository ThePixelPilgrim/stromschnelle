package de.nereide.stromschnelle.ui.common

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import de.nereide.stromschnelle.StromschnelleApp
import de.nereide.stromschnelle.widget.StromschnelleWidget
import kotlinx.coroutines.launch

/**
 * Nudges the home-screen widget to refresh after a mutation that affects it.
 *
 * Runs on the process-wide [de.nereide.stromschnelle.AppContainer.applicationScope]
 * (not a viewModelScope) so the update cannot be cancelled by a screen/ViewModel
 * being torn down right after the mutation.
 */
fun refreshWidget(context: Context) {
    val app = context.applicationContext as? StromschnelleApp ?: return
    app.container.applicationScope.launch {
        try {
            StromschnelleWidget().updateAll(app)
        } catch (t: Throwable) {
            Log.w("StromschnelleWidget", "refreshWidget: updateAll failed", t)
        }
    }
}
