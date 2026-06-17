package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove

data class GameState(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(board = Board.initial)),
    val currentIndex: Int = 0
) {
    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]

    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1

    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1
}

sealed class GameAction {
    data class SquareClick(val position: Position) : GameAction()
    data class PromotionChoice(val move: BoardMove) : GameAction()
    object StepBack : GameAction()
    object StepForward : GameAction()
    data class JumpToHistory(val index: Int) : GameAction()
}
