package com.github.maklumi.catur.domain.chess.board

import com.github.maklumi.catur.domain.chess.piece.Piece
import com.github.maklumi.catur.domain.chess.piece.PieceColor

data class Square(
    val position: Position,
    val piece: Piece? = null
) {

    val file: Int =
        position.file

    val rank: Int =
        position.rank

    val isEmpty: Boolean
        get() = piece == null

    val isNotEmpty: Boolean
        get() = !isEmpty

    fun hasPiece(pieceColor: PieceColor): Boolean =
        piece?.pieceColor == pieceColor

    override fun toString(): String =
        File.entries[file - 1].toString() + rank.toString()
}
