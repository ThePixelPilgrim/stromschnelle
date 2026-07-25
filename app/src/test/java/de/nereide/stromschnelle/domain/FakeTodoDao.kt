package de.nereide.stromschnelle.domain

import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Pure-JVM in-memory [TodoDao] backed by a [MutableStateFlow] map, used to test
 * [DefaultTodoRepository] without Room/Robolectric.
 *
 * The two comparators mirror the SQL in [TodoDao] exactly. If one changes, the
 * other must change with it — this fake is the only place the ordering is
 * asserted in pure JVM tests.
 */
class FakeTodoDao : TodoDao {

    private val state = MutableStateFlow<Map<Long, Todo>>(emptyMap())
    private var nextId = 1L

    private val byImportance = compareByDescending<Todo> { it.completedAt == null }
        .thenByDescending { it.importance }
        .thenBy { it.effort }
        .thenBy { it.createdAt }

    private val byEffort = compareByDescending<Todo> { it.completedAt == null }
        .thenBy { it.effort }
        .thenByDescending { it.importance }
        .thenBy { it.createdAt }

    private fun visibleSorted(cutoff: Long, comparator: Comparator<Todo>): List<Todo> =
        state.value.values
            .filter { it.completedAt == null || it.completedAt >= cutoff }
            .sortedWith(comparator)

    override fun visibleByImportance(cutoff: Long): Flow<List<Todo>> = state.map { todos ->
        todos.values
            .filter { it.completedAt == null || it.completedAt >= cutoff }
            .sortedWith(byImportance)
    }

    override suspend fun visibleByImportanceList(cutoff: Long): List<Todo> =
        visibleSorted(cutoff, byImportance)

    override fun visibleByEffort(cutoff: Long): Flow<List<Todo>> = state.map { todos ->
        todos.values
            .filter { it.completedAt == null || it.completedAt >= cutoff }
            .sortedWith(byEffort)
    }

    override suspend fun visibleByEffortList(cutoff: Long): List<Todo> =
        visibleSorted(cutoff, byEffort)

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

    override suspend fun setImportance(id: Long, value: Int) {
        val current = state.value[id] ?: return
        state.value = state.value + (id to current.copy(importance = value))
    }

    override suspend fun setEffort(id: Long, value: Int) {
        val current = state.value[id] ?: return
        state.value = state.value + (id to current.copy(effort = value))
    }
}
