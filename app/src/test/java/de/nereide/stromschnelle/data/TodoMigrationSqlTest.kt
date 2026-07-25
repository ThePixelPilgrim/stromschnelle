package de.nereide.stromschnelle.data

import java.sql.Connection
import java.sql.DriverManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Drives [TodoMigrationSql.V1_TO_V2] through a real SQLite engine on the JVM.
 *
 * This is not a substitute for the instrumented [MigrationTest] — it cannot
 * validate the result against Room's expected schema. It does cover the part
 * most likely to be wrong: the thirds arithmetic and the column mapping.
 */
class TodoMigrationSqlTest {

    private lateinit var conn: Connection

    @Before
    fun setUp() {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        conn.createStatement().use { it.executeUpdate(TodoMigrationSql.CREATE_V1_FOR_TEST) }
    }

    @After
    fun tearDown() {
        conn.close()
    }

    private fun insertV1(
        id: Long,
        sortIndex: Double,
        title: String = "Todo $id",
        completedAt: Long? = null
    ) {
        conn.prepareStatement(
            "INSERT INTO todos (id, title, description, iconKey, sortIndex, createdAt, completedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ).use { st ->
            st.setLong(1, id)
            st.setString(2, title)
            st.setString(3, "desc $id")
            st.setString(4, "star")
            st.setDouble(5, sortIndex)
            st.setLong(6, 1_000 + id)
            if (completedAt == null) st.setNull(7, java.sql.Types.INTEGER) else st.setLong(7, completedAt)
            st.executeUpdate()
        }
    }

    private fun migrate() {
        conn.createStatement().use { st ->
            TodoMigrationSql.V1_TO_V2.forEach { st.executeUpdate(it) }
        }
    }

    private fun importanceById(): Map<Long, Int> {
        val out = mutableMapOf<Long, Int>()
        conn.createStatement().use { st ->
            st.executeQuery("SELECT id, importance FROM todos ORDER BY id").use { rs ->
                while (rs.next()) out[rs.getLong(1)] = rs.getInt(2)
            }
        }
        return out
    }

    @Test
    fun `nine rows map into thirds by sortIndex rank`() {
        (1L..9L).forEach { insertV1(it, it.toDouble()) }

        migrate()

        assertEquals(
            mapOf(
                1L to 3, 2L to 3, 3L to 3,
                4L to 2, 5L to 2, 6L to 2,
                7L to 1, 8L to 1, 9L to 1
            ),
            importanceById()
        )
    }

    @Test
    fun `thirds follow sortIndex order, not insertion order`() {
        // Inserted worst-first: id 1 has the highest sortIndex and must end up
        // in the BOTTOM third.
        insertV1(1, 90.0)
        insertV1(2, 50.0)
        insertV1(3, 10.0)

        migrate()

        assertEquals(mapOf(1L to 1, 2L to 2, 3L to 3), importanceById())
    }

    @Test
    fun `every row starts at effort 2`() {
        (1L..5L).forEach { insertV1(it, it.toDouble()) }

        migrate()

        conn.createStatement().use { st ->
            st.executeQuery("SELECT DISTINCT effort FROM todos").use { rs ->
                assertTrue(rs.next())
                assertEquals(2, rs.getInt(1))
                assertFalse("effort must be 2 for every row", rs.next())
            }
        }
    }

    @Test
    fun `rows sharing a sortIndex share a rank`() {
        insertV1(1, 5.0)
        insertV1(2, 5.0)
        insertV1(3, 5.0)

        migrate()

        // All three have zero rows below them, so all land in the top third.
        assertEquals(mapOf(1L to 3, 2L to 3, 3L to 3), importanceById())
    }

    @Test
    fun `all other columns survive, including a null completedAt`() {
        insertV1(1, 1.0, title = "Kept", completedAt = 5_555L)
        insertV1(2, 2.0, title = "Open", completedAt = null)

        migrate()

        conn.createStatement().use { st ->
            st.executeQuery(
                "SELECT id, title, description, iconKey, createdAt, completedAt FROM todos ORDER BY id"
            ).use { rs ->
                rs.next()
                assertEquals("Kept", rs.getString(2))
                assertEquals("desc 1", rs.getString(3))
                assertEquals("star", rs.getString(4))
                assertEquals(1_001L, rs.getLong(5))
                assertEquals(5_555L, rs.getLong(6))

                rs.next()
                assertEquals("Open", rs.getString(2))
                rs.getLong(6)
                assertTrue("completedAt must stay NULL", rs.wasNull())
            }
        }
    }

    @Test
    fun `an empty table migrates cleanly`() {
        migrate()

        conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM todos").use { rs ->
                rs.next()
                assertEquals(0, rs.getInt(1))
            }
        }
    }

    @Test
    fun `sortIndex is gone after the migration`() {
        insertV1(1, 1.0)

        migrate()

        conn.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info(todos)").use { rs ->
                val columns = mutableListOf<String>()
                while (rs.next()) columns.add(rs.getString("name"))
                assertFalse(columns.contains("sortIndex"))
                assertTrue(columns.containsAll(listOf("importance", "effort")))
            }
        }
    }
}
