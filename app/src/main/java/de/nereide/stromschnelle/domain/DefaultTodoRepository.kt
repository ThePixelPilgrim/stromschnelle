package de.nereide.stromschnelle.domain

import de.nereide.stromschnelle.data.PriorityRange
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.data.SortMode
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Rotates a priority value, wrapping back to the minimum after the maximum.
 *
 * Lives here rather than in the UI so the app list and the Glance widget share
 * one implementation — two copies would be free to drift.
 */
internal fun nextPriorityValue(current: Int): Int =
    if (current >= PriorityRange.MAX) PriorityRange.MIN else current + 1

class DefaultTodoRepository(
    private val dao: TodoDao,
    private val settings: SettingsRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : TodoRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override val visibleTodos: Flow<List<Todo>> =
        combine(settings.gracePeriodMillis, settings.sortMode, ::Pair)
            .flatMapLatest { (grace, mode) ->
                val cutoff = clock() - grace
                when (mode) {
                    SortMode.IMPORTANCE_FIRST -> dao.visibleByImportance(cutoff)
                    SortMode.EFFORT_FIRST -> dao.visibleByEffort(cutoff)
                }
            }

    override suspend fun visibleTodosNow(): List<Todo> {
        val cutoff = clock() - settings.gracePeriodMillis.first()
        return when (settings.sortMode.first()) {
            SortMode.IMPORTANCE_FIRST -> dao.visibleByImportanceList(cutoff)
            SortMode.EFFORT_FIRST -> dao.visibleByEffortList(cutoff)
        }
    }

    override val completedTodos: Flow<List<Todo>> = dao.completed()

    override fun todo(id: Long): Flow<Todo?> = dao.observe(id)

    override suspend fun add(title: String, description: String, iconKey: String): Long =
        dao.insert(
            Todo(
                title = title,
                description = description,
                iconKey = iconKey,
                createdAt = clock(),
                completedAt = null
            )
        )

    override suspend fun update(todo: Todo) = dao.update(todo)

    override suspend fun complete(id: Long) = dao.setCompletedAt(id, clock())

    override suspend fun uncomplete(id: Long) = dao.setCompletedAt(id, null)

    override suspend fun setImportance(id: Long, value: Int) =
        dao.setImportance(id, value.coerceIn(PriorityRange.MIN, PriorityRange.MAX))

    override suspend fun setEffort(id: Long, value: Int) =
        dao.setEffort(id, value.coerceIn(PriorityRange.MIN, PriorityRange.MAX))

    override suspend fun cycleImportance(id: Long) {
        val current = dao.getById(id) ?: return
        dao.setImportance(id, nextPriorityValue(current.importance))
    }

    override suspend fun cycleEffort(id: Long) {
        val current = dao.getById(id) ?: return
        dao.setEffort(id, nextPriorityValue(current.effort))
    }
}
