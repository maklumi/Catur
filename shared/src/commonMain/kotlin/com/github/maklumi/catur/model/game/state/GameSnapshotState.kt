package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.PieceColor

data class GameSnapshotState(
    val board: Board = Board(),
    val selectedPosition: Position? = null,
    val lastMove: BoardMove? = null,
    val activeColor: PieceColor = PieceColor.WHITE,
    val movedPositions: Set<Position> = emptySet(),
) {
    val legalMoves: List<BoardMove> = selectedPosition?.let { pos ->
        val piece = board[pos].piece
        if (piece?.pieceColor == activeColor) {
            piece.pseudoLegalMoves(board, lastMove, movedPositions).filter { boardMove ->
                val nextBoard = boardMove.move.applyOn(board)
                !nextBoard.isInCheck(activeColor)
            }
        } else {
            null
        }
    } ?: emptyList()

    fun isLegalMove(position: Position): Boolean =
        legalMoves.any { it.move.to == position }

    fun move(to: Position): GameSnapshotState {
        val boardMove = legalMoves.find { it.move.to == to }
        return if (boardMove != null) {
            copy(
                board = boardMove.move.applyOn(board),
                selectedPosition = null,
                lastMove = boardMove,
                activeColor = activeColor.opposite(),
                movedPositions = movedPositions + boardMove.move.from + boardMove.move.to
            )
        } else {
            val piece = board[to].piece
            if (piece != null && piece.pieceColor == activeColor) {
                copy(selectedPosition = to)
            } else {
                copy(selectedPosition = null)
            }
        }
    }
}

fun Board.isInCheck(color: PieceColor): Boolean {
    val kingSquare = findKing(color) ?: return false
    return isAttacked(kingSquare.position, color.opposite())
}
