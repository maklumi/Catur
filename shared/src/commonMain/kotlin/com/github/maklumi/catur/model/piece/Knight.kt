package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.move.BoardMove

class Knight(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♘"
        PieceColor.BLACK -> "♞"
    }

    override val textSymbol: String = "N"

    override val value: Int = 3

    override fun pseudoLegalMoves(board: Board): List<BoardMove> {
        return singleMoves(board, offsets)
    }

    companion object {
        val offsets = listOf(
            -2 to -1,
            -2 to 1,
            -1 to -2,
            -1 to 2,
            1 to -2,
            1 to 2,
            2 to -1,
            2 to 1,
        )
    }
}
