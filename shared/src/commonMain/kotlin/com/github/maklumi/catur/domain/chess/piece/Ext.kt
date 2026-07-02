package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.board.Square
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.move.Move

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

fun Piece.singleMoves(
    board: Board,
    offsets: List<Pair<Int, Int>>,
): List<BoardMove> {
    val moves = mutableListOf<BoardMove>()
    val square = board.find(this) ?: return emptyList()

    offsets.forEach { (deltaFile, deltaRank) ->
        val target = board[square.file + deltaFile, square.rank + deltaRank] ?: return@forEach
        if (target.hasPiece(pieceColor)) return@forEach

        moves += BoardMove(
            Move(piece = this, from = square.position, to = target.position)
        )

    }

    return moves
}

fun Piece.lineAttacks(
    board: Board,
    directions: List<Pair<Int, Int>>,
): List<Position> {
    val attacks = mutableListOf<Position>()
    val square = board.find(this) ?: return emptyList()

    directions.forEach { (deltaFile, deltaRank) ->
        var i = 0
        while (true) {
            i++
            val target = board[square.file + deltaFile * i, square.rank + deltaRank * i] ?: break
            attacks += target.position
            if (target.isNotEmpty) break
        }
    }

    return attacks
}

fun Piece.singleAttacks(
    board: Board,
    offsets: List<Pair<Int, Int>>,
): List<Position> {
    val attacks = mutableListOf<Position>()
    val square = board.find(this) ?: return emptyList()

    offsets.forEach { (deltaFile, deltaRank) ->
        val target = board[square.file + deltaFile, square.rank + deltaRank] ?: return@forEach
        attacks += target.position
    }

    return attacks
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
