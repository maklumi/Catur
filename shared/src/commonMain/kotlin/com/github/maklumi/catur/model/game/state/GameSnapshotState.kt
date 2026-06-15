package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove

data class GameSnapshotState(
    val board: Board = Board(),
    val selectedPosition: Position? = null,
) {
    val legalMoves: List<BoardMove> = selectedPosition?.let {
        board[it].piece?.pseudoLegalMoves(board)
    } ?: emptyList()

    fun isLegalMove(position: Position): Boolean =
        legalMoves.any { it.move.to == position }

    fun move(to: Position): GameSnapshotState {
        val boardMove = legalMoves.find { it.move.to == to }
        return if (boardMove != null) {
            copy(
                board = boardMove.move.applyOn(board),
                selectedPosition = null
            )
        } else {
            val piece = board[to].piece
            if (piece != null) {
                copy(selectedPosition = to)
            } else {
                copy(selectedPosition = null)
            }
        }
    }
}
