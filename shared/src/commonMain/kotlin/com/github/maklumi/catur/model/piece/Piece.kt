package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.move.BoardMove

interface Piece {

    val pieceColor: PieceColor

    val symbol: String

    val textSymbol: String

    val value: Int

    /**
     * List of moves that are legally possible for the piece without applying pin / check constraints
     */
    fun pseudoLegalMoves(board: Board): List<BoardMove>
}