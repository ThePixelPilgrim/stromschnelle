package de.nereide.stromschnelle.domain

import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Pure-JVM in-memory [TodoDao] backed by a [MutableStateFlow] map, used to test
 * [de.nereide.stromschnelle.domain.DefaultTodoRepository] without Room/Robolectric.
 */
class FakeTodoDao : TodoDao {

    private val state = MutableStateFlow<Map<Long, Todo>>(emptyMap())
    private var nextId = 1L

    override fun visible(cutoff: Long): Flow<List<Todo>> = state.map { todos ->
        todos.values
            .filter { it.completedAt == null || it.completedAt >= cutoff }
            .sortedWith(
                compareByDescending<Todo> { it.completedAt == null }
                    .thenBy { it.sortIndex }
            )
    }

    override suspend fun visibleList(cutoff: Long): List<Todo> =
        state.value.values
            .filter { it.completedAt == null || it.completedAt >= cutoff }
            .sortedWith(
                compareByDescending<Todo> { it.completedAt == null }
                    .thenBy { it.sortIndex }
            )

    override fun completed(): Flow<List<Todo>> = state.map { todos ->
        todos.values
            .filter { it.completedAt != null }
            .sortedByDescending { it.completedAt }
    }

    override fun observe(id: Long): Flow<Todo?> = state.map { it[id] }

    override suspend fun getById(id: Long): Todo? = state.value[id]

    override suspend fun insert(todo: Todo): Long {
        val id = if (todo.id != 0L) todo.id else nextId++
        state.value = state.value + (id to todo.copy(id = id))
        return id
    }

    override suspend fun update(todo: Todo) {
        state.value = state.value + (todo.id to todo)
    }

    override suspend fun setCompletedAt(id: Long, ts: Long?) {
        val current = state.value[id] ?: return
        state.value = state.value + (id to current.copy(completedAt = ts))
    }

    override suspend fun maxSortIndex(): Double? = state.value.values.maxOfOrNull { it.sortIndex }
}
