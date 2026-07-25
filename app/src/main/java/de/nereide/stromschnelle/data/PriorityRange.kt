package de.nereide.stromschnelle.data

/**
 * The legal range for [Todo.importance] and [Todo.effort]. Both dimensions
 * share it.
 *
 * Lives in `data` rather than beside the colour palette so the repository can
 * clamp against it without the domain layer depending on `ui.theme`.
 */
object PriorityRange {
    const val MIN = 1
    const val MAX = 3
}
