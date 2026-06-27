package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.CastlingMove
import com.github.maklumi.catur.model.move.EnPassantMove
import com.github.maklumi.catur.model.move.PromotionMove
import com.github.maklumi.catur.model.piece.Pawn
import com.github.maklumi.catur.model.piece.PieceColor

fun Board.getNotation(boardMove: BoardMove, isCheck: Boolean, isMate: Boolean): String {
    val move = boardMove.move
    val piece = move.piece
    
    val base = when (move) {
        is CastlingMove -> {
            if (move.rookTo.file > move.from.file) "O-O" else "O-O-O"
        }
        else -> {
            val piecePrefix = if (piece is Pawn) "" else piece.textSymbol
            val capture = if (this[move.to].isNotEmpty || move is EnPassantMove) {
                if (piece is Pawn) move.from.toString()[0] + "x" else "x"
            } else ""
            
            val promotion = if (move is PromotionMove) "=" + move.promotedPiece.textSymbol else ""
            
            piecePrefix + capture + move.to.toString() + promotion
        }
    }
    
    return base + (if (isMate) "#" else if (isCheck) "+" else "")
}

fun Board.findMoveByNotation(
    notation: String,
    activeColor: PieceColor,
    lastMove: BoardMove?,
    movedPositions: Set<Position>
): BoardMove? {
    val currentPieces = piecesMap.filter { it.value.pieceColor == activeColor }

    // Find all legal moves for the current player
    val allLegalMoves = currentPieces.flatMap { (_, piece) ->
        piece.pseudoLegalMoves(this, lastMove, movedPositions).filter { boardMove ->
            val nextBoard = boardMove.move.applyOn(this)
            !nextBoard.isInCheck(activeColor)
        }
    }

    // Find the one that matches the notation
    return allLegalMoves.find { candidate ->
        val nextBoard = candidate.move.applyOn(this)
        val isCheck = nextBoard.isInCheck(activeColor.opposite())
        val isMate = false // You can implement a full mate check here

        // Use your existing getNotation utility to compare
        getNotation(candidate, isCheck, isMate).replace("+", "").replace("#", "") ==
                notation.replace("+", "").replace("#", "")
    }
}