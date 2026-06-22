package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.EnPassantMove
import com.github.maklumi.catur.model.move.toUciString
import com.github.maklumi.catur.model.piece.Piece
import com.github.maklumi.catur.model.piece.PieceColor

enum class GameStatus {
    ONGOING,
    CHECKMATE,
    STALEMATE,
    WHITE_RESIGNED,
    BLACK_RESIGNED,
    WHITE_OUT_OF_TIME,
    BLACK_OUT_OF_TIME,
    DRAW
}

data class ChessContext(
    val board: Board = Board(),
    val activeColor: PieceColor = PieceColor.WHITE,
    val selectedPosition: Position? = null,
)

data class ChessHistory(
    val lastMove: BoardMove? = null,
    val lastMoveUci: String? = null,
    val movedPositions: Set<Position> = emptySet(),
    val notation: String? = null,
)

data class ChessMaterial(
    val capturedWhite: List<Piece> = emptyList(),
    val capturedBlack: List<Piece> = emptyList(),
)

data class GameSnapshotState(
    val context: ChessContext = ChessContext(),
    val history: ChessHistory = ChessHistory(),
    val material: ChessMaterial = ChessMaterial(),
    val pendingPromotion: List<BoardMove>? = null,
    val forcedStatus: GameStatus? = null,
    val drawOfferedBy: PieceColor? = null,
) {
    // Helpers for cleaner access
    val board get() = context.board
    val activeColor get() = context.activeColor
    val selectedPosition get() = context.selectedPosition
    val lastMove get() = history.lastMove
    val lastMoveUci get() = history.lastMoveUci
    val movedPositions get() = history.movedPositions
    val notation get() = history.notation
    val capturedWhite get() = material.capturedWhite
    val capturedBlack get() = material.capturedBlack

    val legalMoves: List<BoardMove> = if (pendingPromotion != null || forcedStatus != null) emptyList() else selectedPosition?.let { pos ->
        val piece = board[pos].piece
        if (piece?.pieceColor == activeColor) {
            getLegalMovesForPosition(pos)
        } else {
            null
        }
    } ?: emptyList()

    fun getLegalMovesForPosition(pos: Position): List<BoardMove> {
        val piece = board[pos].piece ?: return emptyList()
        return piece.pseudoLegalMoves(board, lastMove, movedPositions).filter { boardMove ->
            val nextBoard = boardMove.move.applyOn(board)
            !nextBoard.isInCheck(piece.pieceColor)
        }
    }

    val status: GameStatus by lazy {
        forcedStatus ?: run {
            val hasAnyLegalMove = board.piecesMap.keys.any { pos ->
                val piece = board[pos].piece
                piece?.pieceColor == activeColor && getLegalMovesForPosition(pos).isNotEmpty()
            }

            if (hasAnyLegalMove) {
                GameStatus.ONGOING
            } else {
                if (board.isInCheck(activeColor)) {
                    GameStatus.CHECKMATE
                } else {
                    GameStatus.STALEMATE
                }
            }
        }
    }

    fun findMoveByUci(uci: String): BoardMove? {
        val candidates = board.piecesMap.keys
            .filter { board[it].piece?.pieceColor == activeColor }
            .flatMap { getLegalMovesForPosition(it) }
            .filter { it.move.toUciString() == uci }
        
        return candidates.firstOrNull()
    }

    fun move(to: Position): GameSnapshotState {
        if (status != GameStatus.ONGOING) return this

        val possibleMoves = legalMoves.filter { it.move.to == to }
        
        return when {
            possibleMoves.size > 1 -> {
                // Promotion case
                copy(pendingPromotion = possibleMoves)
            }
            possibleMoves.size == 1 -> {
                val boardMove = possibleMoves.first()
                applyMove(boardMove)
            }
            else -> {
                val piece = board[to].piece
                if (piece != null && piece.pieceColor == activeColor) {
                    copy(context = context.copy(selectedPosition = to), pendingPromotion = null)
                } else {
                    copy(context = context.copy(selectedPosition = null), pendingPromotion = null)
                }
            }
        }
    }

    fun promote(boardMove: BoardMove): GameSnapshotState {
        return applyMove(boardMove).copy(pendingPromotion = null)
    }

    private fun applyMove(boardMove: BoardMove): GameSnapshotState {
        val move = boardMove.move
        val capturedPiece = when (move) {
            is EnPassantMove -> board[move.capturedPosition].piece
            else -> board[move.to].piece
        }

        val newCapturedWhite = if (capturedPiece?.pieceColor == PieceColor.WHITE) {
            capturedWhite + capturedPiece
        } else capturedWhite

        val newCapturedBlack = if (capturedPiece?.pieceColor == PieceColor.BLACK) {
            capturedBlack + capturedPiece
        } else capturedBlack

        return copy(
            context = context.copy(
                board = move.applyOn(board),
                activeColor = activeColor.opposite(),
                selectedPosition = null,
            ),
            history = history.copy(
                lastMove = boardMove,
                lastMoveUci = move.toUciString(),
                movedPositions = movedPositions + move.from + move.to,
            ),
            material = material.copy(
                capturedWhite = newCapturedWhite,
                capturedBlack = newCapturedBlack,
            )
        )
    }
}

fun Board.isInCheck(color: PieceColor): Boolean {
    val kingSquare = findKing(color) ?: return false
    return isAttacked(kingSquare.position, color.opposite())
}
