package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove

class Rook(override val pieceColor: PieceColor) : Piece() {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♖"
        PieceColor.BLACK -> "♜"
    }

    override val textSymbol: String = "R"

    override val resName: String = if (pieceColor == PieceColor.WHITE) "dubrovny_wr" else "dubrovny_br"

    override val value: Int = 5

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
            -1 to 0,
            1 to 0,
            0 to -1,
            0 to 1,
        )
    }
}
