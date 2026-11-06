package com.zamcan.faceit.data

/** Bounded, immutable edit history. A new edit after undo discards only the redo branch. */
data class EditHistory<T>(
    val current: T,
    private val past: List<T> = emptyList(),
    private val future: List<T> = emptyList()
) {
    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()
    fun record(next: T): EditHistory<T> =
        if (next == current) this else EditHistory(next, (past + current).takeLast(50), emptyList())
    fun undo(): EditHistory<T> = if (!canUndo) this else
        EditHistory(past.last(), past.dropLast(1), (listOf(current) + future).take(50))
    fun redo(): EditHistory<T> = if (!canRedo) this else
        EditHistory(future.first(), (past + current).takeLast(50), future.drop(1))
}

