package de.nereide.stromschnelle.data

import de.nereide.stromschnelle.domain.FakeDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {

    private val settings = SettingsRepository(FakeDataStore())

    @Test
    fun `sortMode defaults to IMPORTANCE_FIRST`() = runTest {
        assertEquals(SortMode.IMPORTANCE_FIRST, settings.sortMode.first())
    }

    @Test
    fun `sortMode round-trips`() = runTest {
        settings.setSortMode(SortMode.EFFORT_FIRST)
        assertEquals(SortMode.EFFORT_FIRST, settings.sortMode.first())
    }

    @Test
    fun `widgetTitle defaults to Aufgaben`() = runTest {
        assertEquals("Aufgaben", settings.widgetTitle.first())
    }

    @Test
    fun `widgetTitle keeps a deliberately blank value`() = runTest {
        settings.setWidgetTitle("")
        assertEquals("", settings.widgetTitle.first())
    }

    @Test
    fun `widgetTitle round-trips a custom value`() = runTest {
        settings.setWidgetTitle("Heute")
        assertEquals("Heute", settings.widgetTitle.first())
    }
}
