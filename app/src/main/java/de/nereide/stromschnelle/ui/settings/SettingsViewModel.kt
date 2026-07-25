package de.nereide.stromschnelle.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.ui.common.appContainer
import de.nereide.stromschnelle.ui.common.application
import de.nereide.stromschnelle.ui.common.refreshWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A selectable grace-period option shown in settings. */
data class GraceOption(val label: String, val millis: Long)

val GraceOptions = listOf(
    GraceOption("1 hour", 60 * 60 * 1_000L),
    GraceOption("6 hours", 6 * 60 * 60 * 1_000L),
    GraceOption("1 day", 24 * 60 * 60 * 1_000L),
    GraceOption("3 days", 3 * 24 * 60 * 60 * 1_000L),
    GraceOption("7 days", 7 * 24 * 60 * 60 * 1_000L)
)

data class SettingsUiState(
    val gracePeriodMillis: Long = SettingsRepository.DEFAULT_GRACE_PERIOD_MILLIS,
    val widgetTitle: String = SettingsRepository.DEFAULT_WIDGET_TITLE
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.gracePeriodMillis,
        settingsRepository.widgetTitle
    ) { grace, title ->
        SettingsUiState(gracePeriodMillis = grace, widgetTitle = title)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setGracePeriod(millis: Long) {
        viewModelScope.launch { settingsRepository.setGracePeriodMillis(millis) }
    }

    fun setWidgetTitle(title: String) {
        viewModelScope.launch {
            settingsRepository.setWidgetTitle(title)
            refreshWidget(context)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = this.appContainer().settingsRepository,
                    context = this.application()
                )
            }
        }
    }
}
