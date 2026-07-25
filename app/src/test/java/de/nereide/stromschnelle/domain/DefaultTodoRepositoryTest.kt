package de.nereide.stromschnelle.domain

import app.cash.turbine.test
import de.nereide.stromschnelle.data.SettingsRepository
import de.nereide.stromschnelle.data.SortMode
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

    /** Adds a todo and immediately stamps it with the given priority pair. */
    private suspend fun addWith(title: String, importance: Int, effort: Int): Long {
        val id = repository.add(title, "", "STAR")
        repository.setImportance(id, importance)
        repository.setEffort(id, effort)
        now += 1_000
        return id
    }

    @Test
    fun `add defaults both dimensions to 2`() = runTest {
        val id = repository.add("First", "", "STAR")
        val todo = dao.getById(id)!!
        assertEquals(now, todo.createdAt)
        assertEquals(2, todo.importance)
        assertEquals(2, todo.effort)
    }

    @Test
    fun `nextPriorityValue rotates and wraps at three`() {
        assertEquals(2, nextPriorityValue(1))
        assertEquals(3, nextPriorityValue(2))
        assertEquals(1, nextPriorityValue(3))
    }

    @Test
    fun `cycleImportance and cycleEffort persist the rotated value`() = runTest {
        val id = repository.add("Task", "", "STAR")

        repository.cycleImportance(id)
        assertEquals(3, dao.getById(id)!!.importance)
        repository.cycleImportance(id)
        assertEquals(1, dao.getById(id)!!.importance)

        repository.cycleEffort(id)
        assertEquals(3, dao.getById(id)!!.effort)
        repository.cycleEffort(id)
        assertEquals(1, dao.getById(id)!!.effort)
    }

    @Test
    fun `importance-first orders by importance desc then effort asc`() = runTest {
        val w2a1 = addWith("W2A1", 2, 1)
        val w3a3 = addWith("W3A3", 3, 3)
        val w3a1 = addWith("W3A1", 3, 1)
        val w1a1 = addWith("W1A1", 1, 1)

        repository.visibleTodos.test {
            assertEquals(listOf(w3a1, w3a3, w2a1, w1a1), awaitItem().map { it.id })
        }
    }

    @Test
    fun `effort-first orders by effort asc then importance desc`() = runTest {
        val w2a1 = addWith("W2A1", 2, 1)
        val w3a3 = addWith("W3A3", 3, 3)
        val w3a1 = addWith("W3A1", 3, 1)
        val w1a2 = addWith("W1A2", 1, 2)

        settings.setSortMode(SortMode.EFFORT_FIRST)

        repository.visibleTodos.test {
            assertEquals(listOf(w3a1, w2a1, w1a2, w3a3), awaitItem().map { it.id })
        }
    }

    @Test
    fun `createdAt ascending breaks ties within an identical pair`() = runTest {
        val older = addWith("Older", 2, 2)
        val newer = addWith("Newer", 2, 2)

        repository.visibleTodos.test {
            assertEquals(listOf(older, newer), awaitItem().map { it.id })
        }
    }

    @Test
    fun `completed lingering todos sink below active ones in both modes`() = runTest {
        settings.setGracePeriodMillis(60_000L)
        val important = addWith("Important", 3, 1)
        val trivial = addWith("Trivial", 1, 3)

        repository.complete(important)

        repository.visibleTodos.test {
            assertEquals(listOf(trivial, important), awaitItem().map { it.id })
        }

        settings.setSortMode(SortMode.EFFORT_FIRST)
        repository.visibleTodos.test {
            assertEquals(listOf(trivial, important), awaitItem().map { it.id })
        }
    }

    @Test
    fun `visibleTodos excludes completed items older than grace cutoff but keeps lingering ones`() =
        runTest {
            settings.setGracePeriodMillis(1_000L)

            val oldId = repository.add("Old", "", "STAR")
            val recentId = repository.add("Recent", "", "FLAG")
            val activeId = repository.add("Active", "", "HOME")

            repository.complete(oldId)
            now += 2_000
            repository.complete(recentId)
            now += 500

            repository.visibleTodos.test {
                val ids = awaitItem().map { it.id }
                assertTrue("active todo must remain visible", ids.contains(activeId))
                assertTrue("recently completed todo must still linger", ids.contains(recentId))
                assertTrue(
                    "old completed todo must be excluded once past grace period",
                    !ids.contains(oldId)
                )
            }
        }

    @Test
    fun `visibleTodosNow honours the current sort mode`() = runTest {
        val w3a3 = addWith("W3A3", 3, 3)
        val w1a1 = addWith("W1A1", 1, 1)

        assertEquals(listOf(w3a3, w1a1), repository.visibleTodosNow().map { it.id })

        settings.setSortMode(SortMode.EFFORT_FIRST)
        assertEquals(listOf(w1a1, w3a3), repository.visibleTodosNow().map { it.id })
    }

    @Test
    fun `complete and uncomplete toggle completedAt and preserve priorities`() = runTest {
        val id = addWith("Task", 3, 1)

        now += 10_000
        repository.complete(id)
        val completed = dao.getById(id)!!
        assertEquals(now, completed.completedAt)
        assertEquals(3, completed.importance)
        assertEquals(1, completed.effort)

        now += 10_000
        repository.uncomplete(id)
        val restored = dao.getById(id)!!
        assertNull(restored.completedAt)
        assertEquals(3, restored.importance)
        assertEquals(1, restored.effort)
    }

    @Test
    fun `no delete api exists, completed todos remain forever`() = runTest {
        val id = repository.add("Persistent", "", "STAR")
        repository.complete(id)

        repository.completedTodos.test {
            assertTrue(awaitItem().any { it.id == id })
        }
        assertNotNull(dao.getById(id))
    }
}
