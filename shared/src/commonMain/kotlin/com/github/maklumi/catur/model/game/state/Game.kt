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

    fun jumpTo(index: Int): Game = if (index in snapshots.indices) copy(currentIndex = index) else this

    fun move(to: Position): Game {
        if (isViewingHistory) return this

        val nextSnapshotBeforeNotation = currentSnapshot.move(to)
        if (nextSnapshotBeforeNotation == currentSnapshot) return this

        return if (nextSnapshotBeforeNotation.board != currentSnapshot.board) {
            val boardMove = nextSnapshotBeforeNotation.lastMove!!
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            
            val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(notation = notation)
            
            copy(
                snapshots = snapshots + nextSnapshot,
                currentIndex = currentIndex + 1
            )
        } else {
            copy(
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshotBeforeNotation) }
            )
        }
    }

    fun promote(move: BoardMove): Game {
        if (isViewingHistory) return this

        val nextSnapshotBeforeNotation = currentSnapshot.promote(move)
        
        val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
        val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
        val notation = currentSnapshot.board.getNotation(move, isCheck, isMate)
        val nextSnapshot = nextSnapshotBeforeNotation.copy(notation = notation)

        return copy(
            snapshots = snapshots + nextSnapshot,
            currentIndex = currentIndex + 1
        )
    }
}
