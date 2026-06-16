package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.move.BoardMove

class King(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♔"
        PieceColor.BLACK -> "♚"
    }

    override val textSymbol: String = "K"

    override val value: Int = 100

    override fun pseudoLegalMoves(board: Board, lastMove: BoardMove?): List<BoardMove> {
        return singleMoves(board, offsets)
    }

    companion object {
        val offsets = listOf(
            -1 to -1,
            -1 to 0,
            -1 to 1,
            0 to -1,
            0 to 1,
            1 to -1,
            1 to 0,
            1 to 1,
        )
    }
}
