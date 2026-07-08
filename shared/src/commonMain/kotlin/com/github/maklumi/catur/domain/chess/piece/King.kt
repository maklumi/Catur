package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove

class King(override val pieceColor: PieceColor) : Piece() {

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
        val pos = board.find(this) ?: return moves

        if (movedPositions.contains(pos)) return moves

        val rank = pos.rank
        val oppositeColor = pieceColor.opposite()

        // Kingside
        val kingsideRookPos = Position.from(8, rank)
        val kingsideRook = board[kingsideRookPos]
        if (kingsideRook is Rook && !movedPositions.contains(kingsideRookPos)) {
            val f = Position.from(6, rank)
            val g = Position.from(7, rank)
            if (board[f] == null && board[g] == null &&
                !board.isAttacked(pos, oppositeColor) &&
                !board.isAttacked(f, oppositeColor) &&
                !board.isAttacked(g, oppositeColor)
            ) {
                moves += BoardMove.Castling(
                    piece = this,
                    from = pos,
                    to = g,
                    rook = kingsideRook,
                    rookFrom = kingsideRookPos,
                    rookTo = f
                )
            }
        }

        // Queenside
        val queensideRookPos = Position.from(1, rank)
        val queensideRook = board[queensideRookPos]
        if (queensideRook is Rook && !movedPositions.contains(queensideRookPos)) {
            val d = Position.from(4, rank)
            val c = Position.from(3, rank)
            val b = Position.from(2, rank)
            if (board[d] == null && board[c] == null && board[b] == null &&
                !board.isAttacked(pos, oppositeColor) &&
                !board.isAttacked(d, oppositeColor) &&
                !board.isAttacked(c, oppositeColor)
            ) {
                moves += BoardMove.Castling(
                    piece = this,
                    from = pos,
                    to = c,
                    rook = queensideRook,
                    rookFrom = queensideRookPos,
                    rookTo = d
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
