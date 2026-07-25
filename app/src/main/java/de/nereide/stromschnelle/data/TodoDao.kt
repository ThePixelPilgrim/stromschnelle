package de.nereide.stromschnelle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Ordering exists in two variants because SQLite cannot bind a column name:
 * `ORDER BY :column` would sort by the literal string, silently. Each variant
 * additionally needs a one-shot twin — see [visibleByImportanceList].
 */
@Dao
interface TodoDao {

    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, importance DESC, effort ASC, createdAt ASC
        """
    )
    fun visibleByImportance(cutoff: Long): Flow<List<Todo>>

    /**
     * One-shot equivalent of [visibleByImportance]. Reading committed rows
     * directly (rather than awaiting a Flow's first emission) avoids Room
     * invalidation timing, so the widget always renders the current state right
     * after a write.
     */
    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, importance DESC, effort ASC, createdAt ASC
        """
    )
    suspend fun visibleByImportanceList(cutoff: Long): List<Todo>

    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, effort ASC, importance DESC, createdAt ASC
        """
    )
    fun visibleByEffort(cutoff: Long): Flow<List<Todo>>

    /** One-shot equivalent of [visibleByEffort]; see [visibleByImportanceList]. */
    @Query(
        """
        SELECT * FROM todos
        WHERE completedAt IS NULL OR completedAt >= :cutoff
        ORDER BY (completedAt IS NULL) DESC, effort ASC, importance DESC, createdAt ASC
        """
    )
    suspend fun visibleByEffortList(cutoff: Long): List<Todo>

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

    @Query("UPDATE todos SET importance = :value WHERE id = :id")
    suspend fun setImportance(id: Long, value: Int)

    @Query("UPDATE todos SET effort = :value WHERE id = :id")
    suspend fun setEffort(id: Long, value: Int)
}
