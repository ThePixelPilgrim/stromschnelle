package de.nereide.stromschnelle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    /**
     * Active todos plus any recently-completed ones still inside the grace window.
     * Active items (completedAt IS NULL) are ordered first, then by [Todo.sortIndex].
     */
    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, sortIndex ASC
        """
    )
    fun visible(cutoff: Long): Flow<List<Todo>>

    /**
     * One-shot equivalent of [visible]. Reading committed rows directly (rather
     * than awaiting a Flow's first emission) avoids Room invalidation timing, so
     * the widget always renders the current state right after a write.
     */
    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, sortIndex ASC
        """
    )
    suspend fun visibleList(cutoff: Long): List<Todo>

    @Query("SELECT * FROM todos WHERE completedAt IS NOT NULL ORDER BY completedAt DESC")
    fun completed(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    fun observe(id: Long): Flow<Todo?>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Long): Todo?

    @Insert
    suspend fun insert(todo: Todo): Long

    @Update
    suspend fun update(todo: Todo)

    /** Sets (or clears) the completion timestamp. Never deletes a row. */
    @Query("UPDATE todos SET completedAt = :ts WHERE id = :id")
    suspend fun setCompletedAt(id: Long, ts: Long?)

    @Query("SELECT MAX(sortIndex) FROM todos")
    suspend fun maxSortIndex(): Double?
}
