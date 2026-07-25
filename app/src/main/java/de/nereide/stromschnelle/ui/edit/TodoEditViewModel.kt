package de.nereide.stromschnelle.ui.edit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoIcon
import de.nereide.stromschnelle.domain.TodoRepository
import de.nereide.stromschnelle.ui.common.appContainer
import de.nereide.stromschnelle.ui.common.application
import de.nereide.stromschnelle.ui.common.refreshWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TodoEditUiState(
    val id: Long? = null,
    val title: String = "",
    val description: String = "",
    val icon: TodoIcon = TodoIcon.STAR,
    val importance: Int = 2,
    val effort: Int = 2,
    val loading: Boolean = false,
    val saved: Boolean = false
)

class TodoEditViewModel(
    private val repository: TodoRepository,
    private val id: Long?,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoEditUiState(id = id, loading = id != null))
    val uiState: StateFlow<TodoEditUiState> = _uiState.asStateFlow()

    init {
        if (id != null) {
            viewModelScope.launch {
                repository.todo(id).collect { todo ->
                    if (todo != null) {
                        _uiState.value = _uiState.value.copy(
                            title = todo.title,
                            description = todo.description,
                            icon = TodoIcon.fromKey(todo.iconKey),
                            importance = todo.importance,
                            effort = todo.effort,
                            loading = false
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onIconChange(icon: TodoIcon) {
        _uiState.value = _uiState.value.copy(icon = icon)
    }

    fun onImportanceChange(value: Int) {
        _uiState.value = _uiState.value.copy(importance = value)
    }

    fun onEffortChange(value: Int) {
        _uiState.value = _uiState.value.copy(effort = value)
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return
        viewModelScope.launch {
            if (id == null) {
                val newId = repository.add(state.title, state.description, state.icon.key)
                repository.setImportance(newId, state.importance)
                repository.setEffort(newId, state.effort)
            } else {
                val current = repository.todo(id).first() ?: return@launch
                repository.update(
                    current.copy(
                        title = state.title,
                        description = state.description,
                        iconKey = state.icon.key,
                        importance = state.importance,
                        effort = state.effort
                    )
                )
            }
            refreshWidget(context)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    companion object {
        fun factory(id: Long?) = viewModelFactory {
            initializer {
                TodoEditViewModel(
                    repository = this.appContainer().todoRepository,
                    id = id,
                    context = this.application()
                )
            }
        }
    }
}
