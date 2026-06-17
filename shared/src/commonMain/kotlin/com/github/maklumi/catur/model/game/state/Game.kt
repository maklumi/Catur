package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove

data class Game(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(board = Board.initial)),
    val currentIndex: Int = 0
) {
    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]

    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1

    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1

    fun goBack(): Game = if (canGoBack()) copy(currentIndex = currentIndex - 1) else this
    fun goForward(): Game = if (canGoForward()) copy(currentIndex = currentIndex + 1) else this

    fun move(to: Position): Game {
        if (isViewingHistory) return this

        val nextSnapshot = currentSnapshot.move(to)
        if (nextSnapshot == currentSnapshot) return this

        // We only add a new snapshot if a move actually occurred
        // Selection changes don't count as a new history entry in most chess apps,
        // but they do update the current state.
        
        return if (nextSnapshot.board != currentSnapshot.board) {
            copy(
                snapshots = snapshots + nextSnapshot,
                currentIndex = currentIndex + 1
            )
        } else {
            // Just update the current selection state without adding to history
            copy(
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
            )
        }
    }

    fun promote(move: BoardMove): Game {
        if (isViewingHistory) return this

        val nextSnapshot = currentSnapshot.promote(move)
        return copy(
            snapshots = snapshots + nextSnapshot,
            currentIndex = currentIndex + 1
        )
    }
}
