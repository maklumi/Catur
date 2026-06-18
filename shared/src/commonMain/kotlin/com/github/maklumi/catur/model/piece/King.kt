package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.CastlingMove

class King(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♔"
        PieceColor.BLACK -> "♚"
    }

    override val textSymbol: String = "K"

    override val resName: String = if (pieceColor == PieceColor.WHITE) "dubrovny_wk" else "dubrovny_bk"

    override val value: Int = 100

    override fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove?,
        movedPositions: Set<Position>
    ): List<BoardMove> {
        val moves = singleMoves(board, offsets).toMutableList()
        val square = board.find(this) ?: return moves

        if (movedPositions.contains(square.position)) return moves

        val rank = square.rank
        val color = pieceColor
        val oppositeColor = color.opposite()

        // Kingside
        val kingsideRookPos = Position.from(8, rank)
        val kingsideRook = board[kingsideRookPos].piece
        if (kingsideRook is Rook && !movedPositions.contains(kingsideRookPos)) {
            val f = Position.from(6, rank)
            val g = Position.from(7, rank)
            if (board[f].isEmpty && board[g].isEmpty &&
                !board.isAttacked(square.position, oppositeColor) &&
                !board.isAttacked(f, oppositeColor) &&
                !board.isAttacked(g, oppositeColor)
            ) {
                moves += BoardMove(
                    CastlingMove(
                        piece = this,
                        from = square.position,
                        to = g,
                        rook = kingsideRook,
                        rookFrom = kingsideRookPos,
                        rookTo = f
                    )
                )
            }
        }

        // Queenside
        val queensideRookPos = Position.from(1, rank)
        val queensideRook = board[queensideRookPos].piece
        if (queensideRook is Rook && !movedPositions.contains(queensideRookPos)) {
            val d = Position.from(4, rank)
            val c = Position.from(3, rank)
            val b = Position.from(2, rank)
            if (board[d].isEmpty && board[c].isEmpty && board[b].isEmpty &&
                !board.isAttacked(square.position, oppositeColor) &&
                !board.isAttacked(d, oppositeColor) &&
                !board.isAttacked(c, oppositeColor)
            ) {
                moves += BoardMove(
                    CastlingMove(
                        piece = this,
                        from = square.position,
                        to = c,
                        rook = queensideRook,
                        rookFrom = queensideRookPos,
                        rookTo = d
                    )
                )
            }
        }

        return moves
    }

    override fun attacks(board: Board): List<Position> {
        return singleAttacks(board, offsets)
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
