package de.nereide.stromschnelle.ui.common

import android.content.Context
import androidx.glance.appwidget.updateAll
import de.nereide.stromschnelle.widget.StromschnelleWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Nudges the home-screen widget to refresh after a mutation that affects it. */
fun refreshWidget(scope: CoroutineScope, context: Context) {
    scope.launch { StromschnelleWidget().updateAll(context) }
}
