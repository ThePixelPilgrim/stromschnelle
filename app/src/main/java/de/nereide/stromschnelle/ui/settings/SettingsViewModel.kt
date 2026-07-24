package de.nereide.stromschnelle.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.ui.common.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

data class SettingsUiState(val gracePeriodMillis: Long = SettingsRepository.DEFAULT_GRACE_PERIOD_MILLIS)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.gracePeriodMillis
        .map { SettingsUiState(gracePeriodMillis = it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState()
        )

    fun setGracePeriod(millis: Long) {
        viewModelScope.launch { settingsRepository.setGracePeriodMillis(millis) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(settingsRepository = this.appContainer().settingsRepository)
            }
        }
    }
}
