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

data class GameSnapshotState(
    val board: Board = Board(),
    val selectedPosition: Position? = null,
    val lastMove: BoardMove? = null,
    val activeColor: PieceColor = PieceColor.WHITE,
    val movedPositions: Set<Position> = emptySet(),
    val pendingPromotion: List<BoardMove>? = null,
    val notation: String? = null,
    val lastMoveUci: String? = null,
    val capturedWhite: List<Piece> = emptyList(),
    val capturedBlack: List<Piece> = emptyList(),
    val forcedStatus: GameStatus? = null,
    val drawOfferedBy: PieceColor? = null,
) {
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

    fun isLegalMove(position: Position): Boolean =
        legalMoves.any { it.move.to == position }

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
                    copy(selectedPosition = to, pendingPromotion = null)
                } else {
                    copy(selectedPosition = null, pendingPromotion = null)
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
            board = move.applyOn(board),
            selectedPosition = null,
            lastMove = boardMove,
            activeColor = activeColor.opposite(),
            movedPositions = movedPositions + move.from + move.to,
            capturedWhite = newCapturedWhite,
            capturedBlack = newCapturedBlack,
            lastMoveUci = move.toUciString()
        )
    }
}

fun Board.isInCheck(color: PieceColor): Boolean {
    val kingSquare = findKing(color) ?: return false
    return isAttacked(kingSquare.position, color.opposite())
}
