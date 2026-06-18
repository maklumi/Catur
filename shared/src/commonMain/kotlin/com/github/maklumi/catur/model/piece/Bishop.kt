package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove

class Bishop(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♗"
        PieceColor.BLACK -> "♝"
    }

    override val textSymbol: String = "B"

    override val resName: String = if (pieceColor == PieceColor.WHITE) "dubrovny_wb" else "dubrovny_bb"

    override val value: Int = 3

    override fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove?,
        movedPositions: Set<Position>
    ): List<BoardMove> {
        return lineMoves(board, directions)
    }

    override fun attacks(board: Board): List<Position> {
        return lineAttacks(board, directions)
    }

    companion object {
        val directions = listOf(
            -1 to -1,
            -1 to 1,
            1 to -1,
            1 to 1,
        )
    }
}