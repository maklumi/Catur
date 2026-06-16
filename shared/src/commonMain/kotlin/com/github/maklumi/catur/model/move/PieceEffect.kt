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

data class EnPassantMove(
    override val piece: Piece,
    override val from: Position,
    override val to: Position,
    val capturedPosition: Position,
) : PrimaryMove {
    override fun applyOn(board: Board): Board {
        return board.copy(
            piecesMap = board.piecesMap
                .minus(from)
                .minus(capturedPosition)
                .plus(to to piece)
        )
    }
}

data class PromotionMove(
    val baseMove: PrimaryMove,
    val promotedPiece: Piece
) : PrimaryMove {
    override val piece: Piece get() = promotedPiece
    override val from: Position get() = baseMove.from
    override val to: Position get() = baseMove.to

    override fun applyOn(board: Board): Board {
        return board.copy(
            piecesMap = board.piecesMap
                .minus(from)
                .plus(to to promotedPiece)
        )
    }
}
