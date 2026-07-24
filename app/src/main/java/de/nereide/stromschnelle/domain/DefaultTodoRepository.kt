package de.nereide.stromschnelle.domain

import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DefaultTodoRepository(
    private val dao: TodoDao,
    private val settings: SettingsRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : TodoRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override val visibleTodos: Flow<List<Todo>> =
        settings.gracePeriodMillis.flatMapLatest { grace ->
            dao.visible(clock() - grace)
        }

    override val completedTodos: Flow<List<Todo>> = dao.completed()

    override fun todo(id: Long): Flow<Todo?> = dao.observe(id)

    override suspend fun add(title: String, description: String, iconKey: String): Long {
        val nextSort = (dao.maxSortIndex() ?: 0.0) + 1.0
        return dao.insert(
            Todo(
                title = title,
                description = description,
                iconKey = iconKey,
                sortIndex = nextSort,
                createdAt = clock(),
                completedAt = null
            )
        )
    }

    override suspend fun update(todo: Todo) = dao.update(todo)

    override suspend fun complete(id: Long) = dao.setCompletedAt(id, clock())

    override suspend fun uncomplete(id: Long) = dao.setCompletedAt(id, null)

    override suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            val current = dao.getById(id) ?: return@forEachIndexed
            dao.update(current.copy(sortIndex = index.toDouble()))
        }
    }
}
