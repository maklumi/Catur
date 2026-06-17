package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.move.CastlingMove
import com.github.maklumi.catur.model.move.EnPassantMove
import com.github.maklumi.catur.model.move.PromotionMove
import com.github.maklumi.catur.model.piece.Pawn

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
