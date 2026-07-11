package com.github.maklumi.catur.state.model

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.board.isInCheck
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.piece.Piece
import com.github.maklumi.catur.domain.chess.piece.PieceColor

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

    val whiteCapturedValue get() = capturedWhite.sumOf { it.value }
    val blackCapturedValue get() = capturedBlack.sumOf { it.value }

    /**
     * Returns positive value if White is ahead, negative if Black is ahead.
     */
    val materialImbalance get() = blackCapturedValue - whiteCapturedValue

    fun generateFen(): String {
        val boardFen = board.toFen()
        val active = if (activeColor == PieceColor.WHITE) "w" else "b"
        // Simplified: assuming no castling/enpassant/clocks for direct engine eval of setup positions
        return "$boardFen $active - - 0 1"
    }

    val legalMoves: List<BoardMove> = if ((pendingPromotion != null) || (forcedStatus != null)) emptyList() else selectedPosition?.let { pos ->
        val piece = board[pos]
        if (piece?.pieceColor == activeColor) {
            getLegalMovesForPosition(pos)
        } else {
            null
        }
    } ?: emptyList()

    fun getLegalMovesForPosition(pos: Position): List<BoardMove> {
        val piece = board[pos] ?: return emptyList()
        return piece.pseudoLegalMoves(board, lastMove, movedPositions).filter { boardMove ->
            val nextBoard = boardMove.applyOn(board)
            !nextBoard.isInCheck(piece.pieceColor)
        }
    }

    val status: GameStatus by lazy {
        forcedStatus ?: run {
            val hasAnyLegalMove = board.piecesMap.keys.any { pos ->
                val piece = board[pos]
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
            .filter { board[it]?.pieceColor == activeColor }
            .flatMap { getLegalMovesForPosition(it) }
            .filter { it.toUciString() == uci }
        
        return candidates.firstOrNull()
    }

    fun move(to: Position): GameSnapshotState {
        if (status != GameStatus.ONGOING) return this

        val possibleMoves = legalMoves.filter { it.to == to }
        
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
                val piece = board[to]
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
        val capturedPiece = when (boardMove) {
            is BoardMove.EnPassant -> board[boardMove.capturedPosition]
            else -> board[boardMove.to]
        }

        val newCapturedWhite = if (capturedPiece?.pieceColor == PieceColor.WHITE) {
            capturedWhite + capturedPiece
        } else capturedWhite

        val newCapturedBlack = if (capturedPiece?.pieceColor == PieceColor.BLACK) {
            capturedBlack + capturedPiece
        } else capturedBlack

        return copy(
            context = context.copy(
                board = boardMove.applyOn(board),
                activeColor = activeColor.opposite(),
                selectedPosition = null,
            ),
            history = history.copy(
                lastMove = boardMove,
                lastMoveUci = boardMove.toUciString(),
                movedPositions = movedPositions + boardMove.from + boardMove.to,
            ),
            material = material.copy(
                capturedWhite = newCapturedWhite,
                capturedBlack = newCapturedBlack,
            )
        )
    }
}
