package com.github.maklumi.catur.domain.chess.move

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.piece.Piece

/**
 * Represents any legal move on the chess board.
 * Consolidates move data, piece effects, and UCI generation.
 */
sealed class BoardMove {
    abstract val piece: Piece
    abstract val from: Position
    abstract val to: Position

    /**
     * Applies this move to a [Board] and returns a new board state.
     */
    abstract fun applyOn(board: Board): Board

    /**
     * Returns the move in UCI format (e.g., "e2e4", "e7e8q").
     */
    fun toUciString(): String {
        val base = "${from}${to}"
        return if (this is Promotion) {
            base + promotedPiece.textSymbol.lowercase()
        } else {
            base
        }
    }

    data class Simple(
        override val piece: Piece,
        override val from: Position,
        override val to: Position
    ) : BoardMove() {
        override fun applyOn(board: Board): Board {
            return board.copy(piecesMap = board.piecesMap - from + (to to piece))
        }
    }

    data class EnPassant(
        override val piece: Piece,
        override val from: Position,
        override val to: Position,
        val capturedPosition: Position
    ) : BoardMove() {
        override fun applyOn(board: Board): Board {
            return board.copy(piecesMap = board.piecesMap - from - capturedPosition + (to to piece))
        }
    }

    data class Promotion(
        val movingPiece: Piece, // The Pawn
        override val from: Position,
        override val to: Position,
        val promotedPiece: Piece
    ) : BoardMove() {
        override val piece: Piece get() = promotedPiece
        override fun applyOn(board: Board): Board {
            return board.copy(piecesMap = board.piecesMap - from + (to to promotedPiece))
        }
    }

    data class Castling(
        override val piece: Piece, // The King
        override val from: Position,
        override val to: Position,
        val rook: Piece,
        val rookFrom: Position,
        val rookTo: Position
    ) : BoardMove() {
        override fun applyOn(board: Board): Board {
            return board.copy(piecesMap = board.piecesMap - from - rookFrom + (to to piece) + (rookTo to rook))
        }
    }
}
