package de.nereide.stromschnelle.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.data.SortMode
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.domain.TodoRepository
import de.nereide.stromschnelle.ui.common.appContainer
import de.nereide.stromschnelle.ui.common.application
import de.nereide.stromschnelle.ui.common.refreshWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val sortMode: SortMode = SettingsRepository.DEFAULT_SORT_MODE,
    val loading: Boolean = true
)

class TodoListViewModel(
    private val repository: TodoRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<TodoListUiState> =
        combine(repository.visibleTodos, settingsRepository.sortMode) { todos, mode ->
            TodoListUiState(todos = todos, sortMode = mode, loading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoListUiState())

    fun toggleSortMode() {
        viewModelScope.launch {
            val next = when (settingsRepository.sortMode.first()) {
                SortMode.IMPORTANCE_FIRST -> SortMode.EFFORT_FIRST
                SortMode.EFFORT_FIRST -> SortMode.IMPORTANCE_FIRST
            }
            settingsRepository.setSortMode(next)
            refreshWidget(context)
        }
    }

    fun toggleComplete(todo: Todo) {
        viewModelScope.launch {
            if (todo.completedAt == null) repository.complete(todo.id) else repository.uncomplete(todo.id)
            refreshWidget(context)
        }
    }

    fun cycleImportance(id: Long) {
        viewModelScope.launch {
            repository.cycleImportance(id)
            refreshWidget(context)
        }
    }

    fun cycleEffort(id: Long) {
        viewModelScope.launch {
            repository.cycleEffort(id)
            refreshWidget(context)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                TodoListViewModel(
                    repository = this.appContainer().todoRepository,
                    settingsRepository = this.appContainer().settingsRepository,
                    context = this.application()
                )
            }
        }
    }
}
