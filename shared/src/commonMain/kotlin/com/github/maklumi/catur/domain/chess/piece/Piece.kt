package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove

interface Piece {

    val pieceColor: PieceColor

    val symbol: String

    val textSymbol: String

    val resName: String

    val value: Int

    /**
     * List of moves that are legally possible for the piece without applying pin / check constraints
     */
    fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove? = null,
        movedPositions: Set<Position> = emptySet()
    ): List<BoardMove>

    /**
     * List of squares currently attacked by this piece
     */
    fun attacks(board: Board): List<Position>
}
