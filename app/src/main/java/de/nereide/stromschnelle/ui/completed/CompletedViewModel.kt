package de.nereide.stromschnelle.ui.completed

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

data class CompletedUiState(val todos: List<Todo> = emptyList())

class CompletedViewModel(
    private val repository: TodoRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<CompletedUiState> = repository.completedTodos
        .map { todos -> CompletedUiState(todos = todos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompletedUiState())

    fun uncomplete(id: Long) {
        viewModelScope.launch {
            repository.uncomplete(id)
            refreshWidget(context)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CompletedViewModel(
                    repository = this.appContainer().todoRepository,
                    context = this.application()
                )
            }
        }
    }
}
