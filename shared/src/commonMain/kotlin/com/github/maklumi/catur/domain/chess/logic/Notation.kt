package com.github.maklumi.catur.domain.chess.logic

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.board.isInCheck
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.move.CastlingMove
import com.github.maklumi.catur.domain.chess.move.EnPassantMove
import com.github.maklumi.catur.domain.chess.move.PromotionMove
import com.github.maklumi.catur.domain.chess.piece.Pawn
import com.github.maklumi.catur.domain.chess.piece.PieceColor

fun Board.getNotation(boardMove: BoardMove, isCheck: Boolean, isMate: Boolean): String {
    val move = boardMove.move
    val piece = if (move is PromotionMove) move.baseMove.piece else move.piece
    
    val base = when (move) {
        is CastlingMove -> {
            if (move.rookTo.file > move.from.file) "O-O" else "O-O-O"
        }
        else -> {
            if (piece is Pawn) {
                val capture = if (this[move.to].isNotEmpty || move is EnPassantMove) {
                    move.from.toString()[0] + "x"
                } else ""
                val promotion = if (move is PromotionMove) "=" + move.promotedPiece.textSymbol else ""
                capture + move.to.toString() + promotion
            } else {
                val piecePrefix = piece.textSymbol
                
                // Disambiguation
                val others = piecesMap.filter { 
                    it.value.pieceColor == piece.pieceColor && 
                    it.value::class == piece::class && 
                    it.key != move.from 
                }
                
                val canAlsoReach = others.filter { (_, p) ->
                    // Simplified check: can it reach the 'to' square? 
                    // To be perfect we'd check legality (not pinned, etc)
                    p.pseudoLegalMoves(this, null, emptySet()).any { it.move.to == move.to }
                }
                
                val disambiguation = if (canAlsoReach.isNotEmpty()) {
                    val sameFile = canAlsoReach.any { it.key.file == move.from.file }
                    val sameRank = canAlsoReach.any { it.key.rank == move.from.rank }
                    
                    when {
                        !sameFile -> move.from.toString()[0].toString()
                        !sameRank -> move.from.toString()[1].toString()
                        else -> move.from.toString()
                    }
                } else ""
                
                val capture = if (this[move.to].isNotEmpty || move is EnPassantMove) "x" else ""
                piecePrefix + disambiguation + capture + move.to.toString()
            }
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
    val clean = notation.replace("+", "").replace("#", "")
    
    // Castling
    if (clean == "O-O" || clean == "0-0") {
        val kingPos = if (activeColor == PieceColor.WHITE) Position.e1 else Position.e8
        val piece = this[kingPos].piece ?: return null
        return piece.pseudoLegalMoves(this, lastMove, movedPositions)
            .find { 
                val m = it.move
                m is CastlingMove && m.rookTo.file > m.from.file 
            }
    }
    if (clean == "O-O-O" || clean == "0-0-0") {
        val kingPos = if (activeColor == PieceColor.WHITE) Position.e1 else Position.e8
        val piece = this[kingPos].piece ?: return null
        return piece.pseudoLegalMoves(this, lastMove, movedPositions)
            .find { 
                val m = it.move
                m is CastlingMove && m.rookTo.file < m.from.file 
            }
    }

    // Parse Target Square
    val cleanNoPromo = clean.replace(Regex("=[QRBN]"), "")
    val promoChar = if (clean.contains("=")) clean.substringAfter("=").take(1) else null
    val targetStr = cleanNoPromo.takeLast(2)
    val target = try { Position.valueOf(targetStr) } catch (_: Exception) { return null }

    // Parse Piece Type
    val firstChar = cleanNoPromo[0]
    val pieceType = if (firstChar.isUpperCase() && firstChar != 'O') firstChar.toString() else ""
    
    // Parse Disambiguation
    val remainder = if (pieceType.isEmpty()) {
        cleanNoPromo.dropLast(2).replace("x", "")
    } else {
        cleanNoPromo.drop(1).dropLast(2).replace("x", "")
    }

    val currentPieces = piecesMap.filter { it.value.pieceColor == activeColor }
    val candidates = currentPieces.flatMap { (_, piece) ->
        val pieceSymbol = if (piece is Pawn) "" else piece.textSymbol
        if (pieceSymbol != pieceType) return@flatMap emptyList<BoardMove>()
        
        piece.pseudoLegalMoves(this, lastMove, movedPositions).filter { boardMove ->
            val m = boardMove.move
            val targetMatch = m.to == target
            val promoMatch = if (promoChar != null && m is PromotionMove) {
                m.promotedPiece.textSymbol == promoChar
            } else true
            
            targetMatch && promoMatch && !m.applyOn(this).isInCheck(activeColor)
        }
    }

    return candidates.find { cand ->
        if (remainder.isEmpty()) return@find true
        val fromStr = cand.move.from.toString()
        if (remainder.length == 1) {
            fromStr.contains(remainder)
        } else {
            fromStr == remainder
        }
    }
}
