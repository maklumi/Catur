package com.github.maklumi.catur.domain.chess.notation

import com.github.maklumi.catur.domain.chess.board.*
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.piece.*

fun Board.getNotation(move: BoardMove, isCheck: Boolean, isMate: Boolean): String {
    val piece = if (move is BoardMove.Promotion) move.movingPiece else move.piece
    
    val base = when (move) {
        is BoardMove.Castling -> {
            if (move.rookTo.file > move.from.file) "O-O" else "O-O-O"
        }
        else -> {
            if (piece is Pawn) {
                val capture = if (this[move.to].isNotEmpty || move is BoardMove.EnPassant) {
                    move.from.toString()[0] + "x"
                } else ""
                val promotion = if (move is BoardMove.Promotion) "=" + move.promotedPiece.textSymbol else ""
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
                    // To be perfect we'd check legality (not pinned, etc.)
                    p.pseudoLegalMoves(this, null, emptySet()).any { it.to == move.to }
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
                
                val capture = if (this[move.to].isNotEmpty || move is BoardMove.EnPassant) "x" else ""
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
        val piece = this[kingPos] ?: return null
        return piece.pseudoLegalMoves(this, lastMove, movedPositions)
            .find { 
                it is BoardMove.Castling && it.rookTo.file > it.from.file 
            }
    }
    if (clean == "O-O-O" || clean == "0-0-0") {
        val kingPos = if (activeColor == PieceColor.WHITE) Position.e1 else Position.e8
        val piece = this[kingPos] ?: return null
        return piece.pseudoLegalMoves(this, lastMove, movedPositions)
            .find { 
                it is BoardMove.Castling && it.rookTo.file < it.from.file 
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
        if (pieceSymbol != pieceType) return@flatMap emptyList()
        
        piece.pseudoLegalMoves(this, lastMove, movedPositions).filter { boardMove ->
            val targetMatch = boardMove.to == target
            val promoMatch = if (promoChar != null && boardMove is BoardMove.Promotion) {
                boardMove.promotedPiece.textSymbol == promoChar
            } else true
            
            targetMatch && promoMatch && !boardMove.applyOn(this).isInCheck(activeColor)
        }
    }

    return candidates.find { candidateMove ->
        if (remainder.isEmpty()) return@find true
        val fromStr = candidateMove.from.toString()
        if (remainder.length == 1) {
            fromStr.contains(remainder)
        } else {
            fromStr == remainder
        }
    }
}
