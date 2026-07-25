package de.nereide.stromschnelle.data

/**
 * The statements that move `todos` from schema 1 to schema 2, in order.
 *
 * Kept separate from [TodoDatabase] so the JDBC-level unit test can run exactly
 * these strings. If you edit one, the test covers it automatically; if you
 * inline SQL back into the migration, that coverage silently disappears.
 */
internal object TodoMigrationSql {

    val V1_TO_V2: List<String> = listOf(
        """
        CREATE TABLE todos_new (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            iconKey TEXT NOT NULL,
            importance INTEGER NOT NULL,
            effort INTEGER NOT NULL,
            createdAt INTEGER NOT NULL,
            completedAt INTEGER
        )
        """.trimIndent(),

        // The existing manual ordering is preserved rather than flattened: the
        // sortIndex ranking is cut into thirds — top third becomes importance 3,
        // middle 2, bottom 1. Effort starts at 2 everywhere.
        //
        // The rank uses a correlated COUNT(*) subquery, NOT NTILE(3). Window
        // functions arrived in SQLite 3.25 and minSdk 26 is Android 8.0 with
        // SQLite 3.19 — NTILE would pass on a modern emulator and crash on an
        // old device.
        """
        INSERT INTO todos_new
            (id, title, description, iconKey, importance, effort, createdAt, completedAt)
        SELECT t.id, t.title, t.description, t.iconKey,
               CASE
                 WHEN (SELECT COUNT(*) FROM todos t2 WHERE t2.sortIndex < t.sortIndex) * 3
                      < (SELECT COUNT(*) FROM todos)     THEN 3
                 WHEN (SELECT COUNT(*) FROM todos t2 WHERE t2.sortIndex < t.sortIndex) * 3
                      < (SELECT COUNT(*) FROM todos) * 2 THEN 2
                 ELSE 1
               END,
               2,
               t.createdAt, t.completedAt
        FROM todos t
        """.trimIndent(),

        "DROP TABLE todos",

        "ALTER TABLE todos_new RENAME TO todos"
    )

    /**
     * Schema 1's `todos` DDL, as Room generated it. Test scaffolding only — the
     * production migration never creates a v1 table.
     */
    val CREATE_V1_FOR_TEST: String = """
        CREATE TABLE todos (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            iconKey TEXT NOT NULL,
            sortIndex REAL NOT NULL,
            createdAt INTEGER NOT NULL,
            completedAt INTEGER
        )
    """.trimIndent()
}
