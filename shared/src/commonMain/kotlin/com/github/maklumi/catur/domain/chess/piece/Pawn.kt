package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove
import kotlin.math.abs

class Pawn(override val pieceColor: PieceColor) : Piece() {

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
            if (oneForward.rank == promotionRank) {
                moves += promotionMoves(square.position, oneForward.position)
            } else {
                moves += BoardMove.Simple(this, square.position, oneForward.position)
            }

            // Forward 2
            if (square.rank == startRank) {
                val twoForward = board[square.file, square.rank + 2 * forward]
                if (twoForward != null && twoForward.isEmpty) {
                    moves += BoardMove.Simple(this, square.position, twoForward.position)
                }
            }
        }

        // Captures
        listOf(-1, 1).forEach { deltaFile ->
            val target = board[square.file + deltaFile, square.rank + forward]
            if (target != null) {
                if (target.hasPiece(pieceColor.opposite())) {
                    if (target.rank == promotionRank) {
                        moves += promotionMoves(square.position, target.position)
                    } else {
                        moves += BoardMove.Simple(this, square.position, target.position)
                    }
                } else if (target.isEmpty) {
                    // En Passant
                    val lastMove = lastMove
                    if (lastMove is BoardMove.Simple &&
                        lastMove.piece is Pawn &&
                        abs(lastMove.from.rank - lastMove.to.rank) == 2 &&
                        lastMove.to.file == target.file &&
                        lastMove.to.rank == square.rank
                    ) {
                        moves += BoardMove.EnPassant(
                            piece = this,
                            from = square.position,
                            to = target.position,
                            capturedPosition = lastMove.to
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

    private fun promotionMoves(from: Position, to: Position): List<BoardMove> {
        return listOf(
            Queen(pieceColor),
            Rook(pieceColor),
            Bishop(pieceColor),
            Knight(pieceColor)
        ).map { BoardMove.Promotion(this, from, to, it) }
    }
}
