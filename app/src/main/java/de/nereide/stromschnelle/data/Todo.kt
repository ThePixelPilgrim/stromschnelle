package de.nereide.stromschnelle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single todo item. Items are never deleted; completion is recorded via
 * [completedAt] so history is retained permanently.
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val iconKey: String,
    val sortIndex: Double,
    val createdAt: Long,
    val completedAt: Long? = null
)
