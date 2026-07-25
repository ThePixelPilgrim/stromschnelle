package de.nereide.stromschnelle.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.domain.TodoRepository
import de.nereide.stromschnelle.ui.common.appContainer
import de.nereide.stromschnelle.ui.common.application
import de.nereide.stromschnelle.ui.common.refreshWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val loading: Boolean = true
)

class TodoListViewModel(
    private val repository: TodoRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<TodoListUiState> = repository.visibleTodos
        .map { todos -> TodoListUiState(todos = todos, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoListUiState())

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
                    context = this.application()
                )
            }
        }
    }
}
