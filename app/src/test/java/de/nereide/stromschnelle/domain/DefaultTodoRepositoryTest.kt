package de.nereide.stromschnelle.domain

import app.cash.turbine.test
import de.nereide.stromschnelle.data.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultTodoRepositoryTest {

    private val dao = FakeTodoDao()
    private val settings = SettingsRepository(FakeDataStore())

    /** Controllable fake clock; tests advance it directly. */
    private var now = 1_000_000L

    private lateinit var repository: DefaultTodoRepository

    @Before
    fun setUp() {
        repository = DefaultTodoRepository(dao, settings, clock = { now })
    }

    @Test
    fun `add sets createdAt and increasing sortIndex`() = runTest {
        val firstId = repository.add("First", "", "STAR")
        val first = dao.getById(firstId)!!
        assertEquals(now, first.createdAt)
        assertEquals(1.0, first.sortIndex, 0.0)

        now += 5_000
        val secondId = repository.add("Second", "", "FLAG")
        val second = dao.getById(secondId)!!
        assertEquals(now, second.createdAt)
        assertTrue(second.sortIndex > first.sortIndex)
    }

    @Test
    fun `complete and uncomplete toggle completedAt and preserve sortIndex`() = runTest {
        val id = repository.add("Task", "", "STAR")
        val original = dao.getById(id)!!
        val originalSort = original.sortIndex

        now += 10_000
        repository.complete(id)
        val completed = dao.getById(id)!!
        assertEquals(now, completed.completedAt)
        assertEquals(originalSort, completed.sortIndex, 0.0)

        now += 10_000
        repository.uncomplete(id)
        val restored = dao.getById(id)!!
        assertNull(restored.completedAt)
        assertEquals(originalSort, restored.sortIndex, 0.0)
    }

    @Test
    fun `visibleTodos excludes completed items older than grace cutoff but keeps lingering ones`() =
        runTest {
            settings.setGracePeriodMillis(1_000L)

            val oldId = repository.add("Old", "", "STAR")
            val recentId = repository.add("Recent", "", "FLAG")
            val activeId = repository.add("Active", "", "HOME")

            // Complete "old" far in the past relative to the eventual "now".
            repository.complete(oldId)

            now += 2_000
            repository.complete(recentId)

            // Advance clock so grace window (1000ms) only still covers "recent".
            now += 500

            repository.visibleTodos.test {
                val visible = awaitItem()
                val ids = visible.map { it.id }
                assertTrue("active todo must remain visible", ids.contains(activeId))
                assertTrue("recently completed todo must still linger", ids.contains(recentId))
                assertTrue(
                    "old completed todo must be excluded once past grace period",
                    !ids.contains(oldId)
                )
            }
        }

    @Test
    fun `reorder rewrites sortIndex to match given id order`() = runTest {
        val a = repository.add("A", "", "STAR")
        val b = repository.add("B", "", "FLAG")
        val c = repository.add("C", "", "HOME")

        repository.reorder(listOf(c, a, b))

        val sortedIds = listOf(dao.getById(c)!!, dao.getById(a)!!, dao.getById(b)!!)
            .sortedBy { it.sortIndex }
            .map { it.id }
        assertEquals(listOf(c, a, b), sortedIds)
    }

    @Test
    fun `no delete api exists, completed todos remain forever`() = runTest {
        val id = repository.add("Persistent", "", "STAR")
        repository.complete(id)

        repository.completedTodos.test {
            val completed = awaitItem()
            assertTrue(completed.any { it.id == id })
        }

        // The Todo row is still retrievable directly from the dao - nothing purged it.
        assertNotNull(dao.getById(id))

        // TodoDao / TodoRepository intentionally expose no delete method; this test
        // documents that guarantee at the repository-behavior level.
    }
}
