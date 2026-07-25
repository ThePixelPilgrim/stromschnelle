package de.nereide.stromschnelle.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityPaletteTest {

    @Test
    fun `importance and effort never share a background in either theme`() {
        listOf(false, true).forEach { dark ->
            val importance = (1..3).map { PriorityPalette.importance(it, dark).background }
            val effort = (1..3).map { PriorityPalette.effort(it, dark).background }
            assertTrue(
                "importance and effort backgrounds overlap (dark=$dark)",
                importance.intersect(effort.toSet()).isEmpty()
            )
        }
    }

    @Test
    fun `all twelve backgrounds are distinct`() {
        val all = listOf(false, true).flatMap { dark ->
            (1..3).map { PriorityPalette.importance(it, dark).background } +
                (1..3).map { PriorityPalette.effort(it, dark).background }
        }
        assertEquals(12, all.toSet().size)
    }

    @Test
    fun `values outside one to three clamp instead of throwing`() {
        assertEquals(PriorityPalette.importance(1, false), PriorityPalette.importance(0, false))
        assertEquals(PriorityPalette.importance(3, false), PriorityPalette.importance(9, false))
        assertEquals(PriorityPalette.effort(1, true), PriorityPalette.effort(-4, true))
    }

    @Test
    fun `known anchor colours are exact`() {
        assertEquals(Color(0xFFC62828), PriorityPalette.importance(3, dark = false).background)
        assertEquals(Color(0xFF14396B), PriorityPalette.effort(3, dark = false).background)
        assertEquals(Color(0xFFEF5350), PriorityPalette.importance(3, dark = true).background)
        assertEquals(Color(0xFF8AB9F0), PriorityPalette.effort(3, dark = true).background)
    }
}
