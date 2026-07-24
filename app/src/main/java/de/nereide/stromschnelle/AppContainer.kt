package de.nereide.stromschnelle

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.data.TodoDatabase
import de.nereide.stromschnelle.domain.DefaultTodoRepository
import de.nereide.stromschnelle.domain.TodoRepository

/** Service-locator container exposing the app's singletons. No Hilt. */
interface AppContainer {
    val todoRepository: TodoRepository
    val settingsRepository: SettingsRepository
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext.settingsDataStore)
    }

    override val todoRepository: TodoRepository by lazy {
        DefaultTodoRepository(
            dao = TodoDatabase.getInstance(appContext).todoDao(),
            settings = settingsRepository,
            clock = System::currentTimeMillis
        )
    }
}
