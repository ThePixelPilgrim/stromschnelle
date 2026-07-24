package de.nereide.stromschnelle.domain

import de.nereide.stromschnelle.data.Todo
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing access to todos. There is intentionally no delete: completed
 * todos are retained permanently and merely hidden once the grace period lapses.
 */
interface TodoRepository {

    /** Active todos plus recently-completed ones inside the settings grace window. */
    val visibleTodos: Flow<List<Todo>>

    /** All completed todos, newest completion first. */
    val completedTodos: Flow<List<Todo>>

    fun todo(id: Long): Flow<Todo?>

    suspend fun add(title: String, description: String, iconKey: String): Long

    suspend fun update(todo: Todo)

    suspend fun complete(id: Long)

    suspend fun uncomplete(id: Long)

    suspend fun reorder(orderedIds: List<Long>)
}
