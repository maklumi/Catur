package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.move.BoardMove

class Queen(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♕"
        PieceColor.BLACK -> "♛"
    }

    override val textSymbol: String = "Q"

    override val value: Int = 9

    override fun pseudoLegalMoves(board: Board, lastMove: BoardMove?): List<BoardMove> {
        return lineMoves(board, directions)
    }

    companion object {
        val directions = listOf(
            -1 to -1,
            -1 to 1,
            1 to -1,
            1 to 1,
            -1 to 0,
            1 to 0,
            0 to -1,
            0 to 1,
        )
    }
}
