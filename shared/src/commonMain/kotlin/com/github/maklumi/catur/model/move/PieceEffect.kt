package com.github.maklumi.catur.model.move

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.piece.Piece


sealed interface PieceEffect {

    val piece: Piece

    fun applyOn(board: Board): Board
}

sealed interface PreMove : PieceEffect

sealed interface PrimaryMove : PieceEffect {

    val from: Position

    val to: Position
}

sealed interface Consequence : PieceEffect

data class Move(
    override val piece: Piece,
    override val from: Position,
    override val to: Position
) : PrimaryMove, Consequence {

    constructor(
        piece: Piece,
        intent: MoveIntention,
    ) : this(
        piece = piece,
        from = intent.from,
        to = intent.to
    )

    override fun applyOn(board: Board): Board {
        return board.copy(
            piecesMap = board.piecesMap
                .minus(from)
                .plus(to to piece)
        )
    }
}