package de.nereide.stromschnelle.data

/**
 * Which of the two priority dimensions dominates the list order.
 *
 * The secondary key is always the other dimension, so the two modes are the
 * same comparison with the keys swapped. See [TodoDao] for the exact clauses.
 */
enum class SortMode {
    /** Importance descending, then effort ascending. */
    IMPORTANCE_FIRST,

    /** Effort ascending, then importance descending. */
    EFFORT_FIRST
}
