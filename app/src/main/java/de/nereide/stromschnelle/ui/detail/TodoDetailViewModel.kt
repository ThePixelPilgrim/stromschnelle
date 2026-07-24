package de.nereide.stromschnelle.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.domain.TodoRepository
import de.nereide.stromschnelle.ui.common.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoDetailUiState(
    val todo: Todo? = null,
    val loading: Boolean = true
)

class TodoDetailViewModel(
    private val repository: TodoRepository,
    private val id: Long
) : ViewModel() {

    val uiState: StateFlow<TodoDetailUiState> = repository.todo(id)
        .map { todo -> TodoDetailUiState(todo = todo, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoDetailUiState())

    fun uncomplete() {
        viewModelScope.launch { repository.uncomplete(id) }
    }

    companion object {
        fun factory(id: Long) = viewModelFactory {
            initializer {
                TodoDetailViewModel(repository = this.appContainer().todoRepository, id = id)
            }
        }
    }
}
