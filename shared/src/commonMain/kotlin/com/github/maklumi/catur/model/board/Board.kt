package com.github.maklumi.catur.model.board

import com.github.maklumi.catur.model.piece.Bishop
import com.github.maklumi.catur.model.piece.Knight
import com.github.maklumi.catur.model.piece.Piece
import com.github.maklumi.catur.model.piece.PieceColor
import java.lang.IllegalArgumentException

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

    fun withPiece(position: Position, piece: Piece?): Board {
        return copy(
            piecesMap = if (piece == null) piecesMap - position else piecesMap + (position to piece)
        )
    }

    companion object {
        val initial = Board(initialPieces)
    }

}

private val initialPieces = mapOf(
    Position.b8 to Knight(PieceColor.BLACK),
    Position.g8 to Knight(PieceColor.BLACK),
    Position.c8 to Bishop(PieceColor.BLACK),
    Position.f8 to Bishop(PieceColor.BLACK),
    Position.b1 to Knight(PieceColor.WHITE),
    Position.g1 to Knight(PieceColor.WHITE),
    Position.c1 to Bishop(PieceColor.WHITE),
    Position.f1 to Bishop(PieceColor.WHITE),
)
