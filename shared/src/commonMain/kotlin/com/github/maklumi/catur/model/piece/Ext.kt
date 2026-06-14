package com.github.maklumi.catur.model.piece

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Square
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.Move

fun Piece.lineMoves(
    board: Board,
    directions: List<Pair<Int, Int>>,
): List<BoardMove> {
    val moves = mutableListOf<BoardMove>()
    val square = board.find(this) ?: return emptyList()

    directions.forEach {
        moves += lineMoves(board, square, it.first, it.second)
    }

    return moves
}

fun lineMoves(
    board: Board,
    square: Square,
    deltaFile: Int,
    deltaRank: Int
): List<BoardMove> {
    requireNotNull(square.piece)
    val pieceColor = square.piece.pieceColor
    val moves = mutableListOf<BoardMove>()

    var i = 0
    while (true) {
        i++
        val target = board[square.file + deltaFile * i, square.rank + deltaRank * i] ?: break
        if (target.hasPiece(pieceColor)) {
            break
        }

        val move = Move(piece = square.piece, from = square.position, to = target.position)
        if (target.isEmpty) {
            moves += BoardMove(move)
            continue
        }
        if (target.hasPiece(pieceColor.opposite())) {
            moves += BoardMove(
                move = move,
            )
            break
        }
    }

    return moves
}