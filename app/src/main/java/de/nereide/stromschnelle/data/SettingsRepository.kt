package de.nereide.stromschnelle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists user settings via DataStore preferences: the grace period that keeps
 * completed todos visible, the list ordering, and the widget's header title.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val gracePeriodMillis: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_GRACE_PERIOD] ?: DEFAULT_GRACE_PERIOD_MILLIS
    }

    suspend fun setGracePeriodMillis(v: Long) {
        dataStore.edit { prefs -> prefs[KEY_GRACE_PERIOD] = v }
    }

    /**
     * An absent or unrecognised stored value falls back to the default rather
     * than throwing, mirroring how [TodoIcon.fromKey] handles unknown keys.
     */
    val sortMode: Flow<SortMode> = dataStore.data.map { prefs ->
        prefs[KEY_SORT_MODE]
            ?.let { name -> SortMode.entries.firstOrNull { it.name == name } }
            ?: DEFAULT_SORT_MODE
    }

    suspend fun setSortMode(mode: SortMode) {
        dataStore.edit { prefs -> prefs[KEY_SORT_MODE] = mode.name }
    }

    /**
     * The widget header title. The default applies only while the key has never
     * been written — a deliberately blank value is preserved and means "render
     * no title", otherwise the title could never be removed.
     */
    val widgetTitle: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_TITLE] ?: DEFAULT_WIDGET_TITLE
    }

    suspend fun setWidgetTitle(title: String) {
        dataStore.edit { prefs -> prefs[KEY_WIDGET_TITLE] = title }
    }

    companion object {
        const val DEFAULT_GRACE_PERIOD_MILLIS: Long = 86_400_000L // 24h
        const val DEFAULT_WIDGET_TITLE: String = "Aufgaben"
        val DEFAULT_SORT_MODE: SortMode = SortMode.IMPORTANCE_FIRST

        private val KEY_GRACE_PERIOD = longPreferencesKey("grace_period_millis")
        private val KEY_SORT_MODE = stringPreferencesKey("sort_mode")
        private val KEY_WIDGET_TITLE = stringPreferencesKey("widget_title")
    }
}
