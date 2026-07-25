package de.nereide.stromschnelle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single todo item. Items are never deleted; completion is recorded via
 * [completedAt] so history is retained permanently.
 *
 * List order is derived entirely from [importance] and [effort] — there is no
 * stored position. Both are in `1..3`; only rotation and the edit screen's
 * segmented buttons write them, and both are bounded by construction.
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val iconKey: String,
    val importance: Int = 2,
    val effort: Int = 2,
    val createdAt: Long,
    val completedAt: Long? = null
)
