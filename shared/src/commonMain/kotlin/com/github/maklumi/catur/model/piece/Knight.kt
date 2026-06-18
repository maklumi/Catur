package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove

class Knight(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♘"
        PieceColor.BLACK -> "♞"
    }

    override val textSymbol: String = "N"

    override val resName: String = if (pieceColor == PieceColor.WHITE) "dubrovny_wn" else "dubrovny_bn"

    override val value: Int = 3

    override fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove?,
        movedPositions: Set<Position>
    ): List<BoardMove> {
        return singleMoves(board, offsets)
    }

    override fun attacks(board: Board): List<Position> {
        return singleAttacks(board, offsets)
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
