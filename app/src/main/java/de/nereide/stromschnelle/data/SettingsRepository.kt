package de.nereide.stromschnelle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists user settings via DataStore preferences. Currently just the grace
 * period controlling how long completed todos stay visible.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val gracePeriodMillis: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_GRACE_PERIOD] ?: DEFAULT_GRACE_PERIOD_MILLIS
    }

    suspend fun setGracePeriodMillis(v: Long) {
        dataStore.edit { prefs -> prefs[KEY_GRACE_PERIOD] = v }
    }

    companion object {
        const val DEFAULT_GRACE_PERIOD_MILLIS: Long = 86_400_000L // 24h
        private val KEY_GRACE_PERIOD = longPreferencesKey("grace_period_millis")
    }
}
