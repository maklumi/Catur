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
        val pos = board.find(this) ?: return emptyList()
        val forward = if (pieceColor == PieceColor.WHITE) 1 else -1
        val startRank = if (pieceColor == PieceColor.WHITE) 2 else 7
        val promotionRank = if (pieceColor == PieceColor.WHITE) 8 else 1

        // Forward 1
        val oneForward = board[pos.file, pos.rank + forward]
        if (oneForward == null && pos.rank + forward in 1..8) {
            val toPos = Position.from(pos.file, pos.rank + forward)
            if (toPos.rank == promotionRank) {
                moves += promotionMoves(pos, toPos)
            } else {
                moves += BoardMove.Simple(this, pos, toPos)
            }

            // Forward 2
            if (pos.rank == startRank) {
                val twoForward = board[pos.file, pos.rank + 2 * forward]
                if (twoForward == null) {
                    moves += BoardMove.Simple(this, pos, Position.from(pos.file, pos.rank + 2 * forward))
                }
            }
        }

        // Captures
        listOf(-1, 1).forEach { deltaFile ->
            val targetFile = pos.file + deltaFile
            val targetRank = pos.rank + forward
            if (targetFile in 1..8 && targetRank in 1..8) {
                val targetPiece = board[targetFile, targetRank]
                val toPos = Position.from(targetFile, targetRank)
                if (targetPiece?.pieceColor == pieceColor.opposite()) {
                    if (targetRank == promotionRank) {
                        moves += promotionMoves(pos, toPos)
                    } else {
                        moves += BoardMove.Simple(this, pos, toPos)
                    }
                } else if (targetPiece == null) {
                    // En Passant
                    val lastMove = lastMove
                    if (lastMove is BoardMove.Simple &&
                        lastMove.piece is Pawn &&
                        abs(lastMove.from.rank - lastMove.to.rank) == 2 &&
                        lastMove.to.file == targetFile &&
                        lastMove.to.rank == pos.rank
                    ) {
                        moves += BoardMove.EnPassant(
                            piece = this,
                            from = pos,
                            to = toPos,
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
        val pos = board.find(this) ?: return emptyList()
        val forward = if (pieceColor == PieceColor.WHITE) 1 else -1

        listOf(-1, 1).forEach { deltaFile ->
            val targetFile = pos.file + deltaFile
            val targetRank = pos.rank + forward
            if (targetFile in 1..8 && targetRank in 1..8) {
                attacks += Position.from(targetFile, targetRank)
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
