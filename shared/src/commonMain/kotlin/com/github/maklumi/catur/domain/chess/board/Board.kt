package com.github.maklumi.catur.domain.chess.board

import com.github.maklumi.catur.domain.chess.piece.Bishop
import com.github.maklumi.catur.domain.chess.piece.King
import com.github.maklumi.catur.domain.chess.piece.Knight
import com.github.maklumi.catur.domain.chess.piece.Pawn
import com.github.maklumi.catur.domain.chess.piece.Piece
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.domain.chess.piece.Queen
import com.github.maklumi.catur.domain.chess.piece.Rook

data class Board(
    val piecesMap: Map<Position, Piece> = emptyMap()
) {

    val squares = Position.entries.associateWith { position ->
        Square(position, piecesMap[position])
    }

    operator fun get(position: Position): Square =
        squares[position]!!

    operator fun get(file: File, rank: Int): Square? =
        get(file.ordinal + 1, rank)

    operator fun get(file: Int, rank: Int): Square? {
        return try {
            val position = Position.from(file, rank)
            squares[position]
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun find(piece: Piece): Square? =
        squares.values.firstOrNull { it.piece == piece }

    fun findKing(color: PieceColor): Square? =
        squares.values.firstOrNull { it.piece is King && it.piece.pieceColor == color }

    fun isAttacked(position: Position, byColor: PieceColor): Boolean {
        return squares.values.any { square ->
            val piece = square.piece
            piece != null && piece.pieceColor == byColor && piece.attacks(this).contains(position)
        }
    }

    fun toFen(): String {
        val fen = StringBuilder()
        for (rank in 8 downTo 1) {
            var emptyCount = 0
            for (file in 1..8) {
                val piece = piecesMap[Position.from(file, rank)]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount)
                        emptyCount = 0
                    }
                    val symbol = piece.textSymbol
                    fen.append(if (piece.pieceColor == PieceColor.WHITE) symbol.uppercase() else symbol.lowercase())
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount)
            }
            if (rank > 1) {
                fen.append("/")
            }
        }
        return fen.toString()
    }

    companion object {
        val initialPieces = mapOf(
            Position.a8 to Rook(PieceColor.BLACK),
            Position.b8 to Knight(PieceColor.BLACK),
            Position.c8 to Bishop(PieceColor.BLACK),
            Position.d8 to Queen(PieceColor.BLACK),
            Position.e8 to King(PieceColor.BLACK),
            Position.f8 to Bishop(PieceColor.BLACK),
            Position.g8 to Knight(PieceColor.BLACK),
            Position.h8 to Rook(PieceColor.BLACK),
            Position.a7 to Pawn(PieceColor.BLACK),
            Position.b7 to Pawn(PieceColor.BLACK),
            Position.c7 to Pawn(PieceColor.BLACK),
            Position.d7 to Pawn(PieceColor.BLACK),
            Position.e7 to Pawn(PieceColor.BLACK),
            Position.f7 to Pawn(PieceColor.BLACK),
            Position.g7 to Pawn(PieceColor.BLACK),
            Position.h7 to Pawn(PieceColor.BLACK),
            Position.a2 to Pawn(PieceColor.WHITE),
            Position.b2 to Pawn(PieceColor.WHITE),
            Position.c2 to Pawn(PieceColor.WHITE),
            Position.d2 to Pawn(PieceColor.WHITE),
            Position.e2 to Pawn(PieceColor.WHITE),
            Position.f2 to Pawn(PieceColor.WHITE),
            Position.g2 to Pawn(PieceColor.WHITE),
            Position.h2 to Pawn(PieceColor.WHITE),
            Position.a1 to Rook(PieceColor.WHITE),
            Position.b1 to Knight(PieceColor.WHITE),
            Position.c1 to Bishop(PieceColor.WHITE),
            Position.d1 to Queen(PieceColor.WHITE),
            Position.e1 to King(PieceColor.WHITE),
            Position.f1 to Bishop(PieceColor.WHITE),
            Position.g1 to Knight(PieceColor.WHITE),
            Position.h1 to Rook(PieceColor.WHITE),
        )

        val initial = Board(initialPieces)

        /**
         * Creates a Board from a FEN string piece placement part.
         * Note: Currently only parses piece placement.
         */
        fun fromFen(fen: String): Board {
            val piecePlacement = fen.split(" ").firstOrNull() ?: return initial
            val piecesMap = mutableMapOf<Position, Piece>()

            val ranks = piecePlacement.split("/")
            if (ranks.size != 8) return initial

            ranks.forEachIndexed { index, rankStr ->
                val rank = 8 - index
                var file = 1

                rankStr.forEach { char ->
                    if (char.isDigit()) {
                        file += char.digitToInt()
                    } else {
                        val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                        val piece = when (char.lowercaseChar()) {
                            'p' -> Pawn(color)
                            'r' -> Rook(color)
                            'n' -> Knight(color)
                            'b' -> Bishop(color)
                            'q' -> Queen(color)
                            'k' -> King(color)
                            else -> null
                        }

                        if (piece != null) {
                            try {
                                piecesMap[Position.from(file, rank)] = piece
                            } catch (_: Exception) {
                            }
                        }
                        file++
                    }
                }
            }
            return Board(piecesMap)
        }

        fun parseActiveColor(fen: String): PieceColor {
            val parts = fen.split(" ")
            return if (parts.size > 1 && parts[1] == "b") PieceColor.BLACK else PieceColor.WHITE
        }
    }

}

fun Board.isInCheck(color: PieceColor): Boolean {
    val kingSquare = findKing(color) ?: return false
    return isAttacked(kingSquare.position, color.opposite())
}
