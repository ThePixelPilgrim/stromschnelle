package de.nereide.stromschnelle.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test.db"

/**
 * What [TodoMigrationSqlTest] cannot do: validate the migrated table against the
 * schema Room expects. `runMigrationsAndValidate` fails here on a type,
 * nullability or primary-key mismatch, rather than on the user's device.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_producesASchemaRoomAccepts() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            (1L..9L).forEach { id ->
                db.execSQL(
                    "INSERT INTO todos (id, title, description, iconKey, sortIndex, createdAt, completedAt) " +
                        "VALUES ($id, 'Todo $id', 'desc $id', 'star', ${id.toDouble()}, ${1_000 + id}, NULL)"
                )
            }
        }

        // validateDroppedTables = true: this is the assertion that matters here.
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)

        db.query("SELECT COUNT(*) FROM todos WHERE importance = 3").use { c ->
            c.moveToFirst()
            assertEquals(3, c.getInt(0))
        }
    }

    @Test
    fun migrate1To2_acceptsAnEmptyDatabase() {
        helper.createDatabase(TEST_DB, 1).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)

        db.query("SELECT COUNT(*) FROM todos").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
    }
}
