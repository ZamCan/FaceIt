package com.zamcan.faceit.data

import org.junit.Assert.*
import org.junit.Test

class EditHistoryTest {
    @Test fun undoAndRedoRestoreIndependentLayers() {
        val original = PersonalEnvironment(selectedArtworkFile = "mine.png")
        val edited = original.copy(material = "Glass")
        val history = EditHistory(original).record(edited)
        assertEquals(original, history.undo().current)
        assertEquals(edited, history.undo().redo().current)
        assertEquals("mine.png", history.current.selectedArtworkFile)
    }
    @Test fun newEditDropsOnlyTheRedoBranch() {
        val history = EditHistory("a").record("b").record("c").undo().record("d")
        assertFalse(history.canRedo)
        assertEquals("b", history.undo().current)
        assertEquals("a", history.undo().undo().current)
    }
    @Test fun noOpEditDoesNotConsumeHistory() {
        assertFalse(EditHistory("a").record("a").canUndo)
    }
    @Test fun historyIsBoundedForLongEditingSessions() {
        var history = EditHistory(0)
        repeat(1000) { history = history.record(it + 1) }
        var count = 0
        while (history.canUndo) { history = history.undo(); count++ }
        assertEquals(50, count)
        assertEquals(950, history.current)
    }
}

