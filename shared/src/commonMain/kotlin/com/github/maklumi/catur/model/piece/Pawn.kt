package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.EnPassantMove
import com.github.maklumi.catur.model.move.Move
import com.github.maklumi.catur.model.move.PromotionMove
import kotlin.math.abs

class Pawn(override val pieceColor: PieceColor) : Piece {

    override val symbol: String = when (pieceColor) {
        PieceColor.WHITE -> "♙"
        PieceColor.BLACK -> "♟"
    }

    override val textSymbol: String = "P"

    override val resName: String = if (pieceColor == PieceColor.WHITE) "dubrovny_wp" else "dubrovny_bp"

    override val value: Int = 1

    override fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove?,
        movedPositions: Set<Position>
    ): List<BoardMove> {
        val moves = mutableListOf<BoardMove>()
        val square = board.find(this) ?: return emptyList()
        val forward = if (pieceColor == PieceColor.WHITE) 1 else -1
        val startRank = if (pieceColor == PieceColor.WHITE) 2 else 7
        val promotionRank = if (pieceColor == PieceColor.WHITE) 8 else 1

        // Forward 1
        val oneForward = board[square.file, square.rank + forward]
        if (oneForward != null && oneForward.isEmpty) {
            val move = Move(this, square.position, oneForward.position)
            if (oneForward.rank == promotionRank) {
                moves += promotionMoves(move)
            } else {
                moves += BoardMove(move)
            }

            // Forward 2
            if (square.rank == startRank) {
                val twoForward = board[square.file, square.rank + 2 * forward]
                if (twoForward != null && twoForward.isEmpty) {
                    moves += BoardMove(Move(this, square.position, twoForward.position))
                }
            }
        }

        // Captures
        listOf(-1, 1).forEach { deltaFile ->
            val target = board[square.file + deltaFile, square.rank + forward]
            if (target != null) {
                if (target.hasPiece(pieceColor.opposite())) {
                    val move = Move(this, square.position, target.position)
                    if (target.rank == promotionRank) {
                        moves += promotionMoves(move)
                    } else {
                        moves += BoardMove(move)
                    }
                } else if (target.isEmpty) {
                    // En Passant
                    val lastMovePrimary = lastMove?.move
                    if (lastMovePrimary is Move &&
                        lastMovePrimary.piece is Pawn &&
                        abs(lastMovePrimary.from.rank - lastMovePrimary.to.rank) == 2 &&
                        lastMovePrimary.to.file == target.file &&
                        lastMovePrimary.to.rank == square.rank
                    ) {
                        moves += BoardMove(
                            EnPassantMove(
                                piece = this,
                                from = square.position,
                                to = target.position,
                                capturedPosition = lastMovePrimary.to
                            )
                        )
                    }
                }
            }
        }

        return moves
    }

    override fun attacks(board: Board): List<Position> {
        val attacks = mutableListOf<Position>()
        val square = board.find(this) ?: return emptyList()
        val forward = if (pieceColor == PieceColor.WHITE) 1 else -1

        listOf(-1, 1).forEach { deltaFile ->
            val target = board[square.file + deltaFile, square.rank + forward]
            if (target != null) {
                attacks += target.position
            }
        }

        return attacks
    }

    private fun promotionMoves(baseMove: Move): List<BoardMove> {
        return listOf(
            Queen(pieceColor),
            Rook(pieceColor),
            Bishop(pieceColor),
            Knight(pieceColor)
        ).map { BoardMove(PromotionMove(baseMove, it)) }
    }
}
