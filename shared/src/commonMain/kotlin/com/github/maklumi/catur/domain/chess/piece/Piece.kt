package com.github.maklumi.catur.domain.chess.piece

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.move.BoardMove

sealed class Piece {
    abstract val pieceColor: PieceColor
    abstract val symbol: String
    abstract val textSymbol: String
    abstract val resName: String
    abstract val value: Int

    /**
     * List of moves that are legally possible for the piece without applying pin / check constraints
     */
    abstract fun pseudoLegalMoves(
        board: Board,
        lastMove: BoardMove? = null,
        movedPositions: Set<Position> = emptySet()
    ): List<BoardMove>

    /**
     * List of squares currently attacked by this piece
     */
    abstract fun attacks(board: Board): List<Position>

    // --- Protected Movement Helpers ---

    protected fun lineMoves(board: Board, directions: List<Pair<Int, Int>>): List<BoardMove> {
        val moves = mutableListOf<BoardMove>()
        val fromPos = board.find(this) ?: return emptyList()
        directions.forEach { (df, dr) ->
            var i = 0
            while (true) {
                i++
                val targetFile = fromPos.file + df * i
                val targetRank = fromPos.rank + dr * i
                val targetPiece = board[targetFile, targetRank] ?: if (targetFile !in 1..8 || targetRank !in 1..8) break else null
                
                if (targetPiece?.pieceColor == pieceColor) break
                
                val toPos = Position.from(targetFile, targetRank)
                moves += BoardMove.Simple(this, fromPos, toPos)
                
                if (targetPiece != null) break
            }
        }
        return moves
    }

    protected fun singleMoves(board: Board, offsets: List<Pair<Int, Int>>): List<BoardMove> {
        val moves = mutableListOf<BoardMove>()
        val fromPos = board.find(this) ?: return emptyList()
        offsets.forEach { (df, dr) ->
            val targetFile = fromPos.file + df
            val targetRank = fromPos.rank + dr
            if (targetFile in 1..8 && targetRank in 1..8) {
                val targetPiece = board[targetFile, targetRank]
                if (targetPiece?.pieceColor != pieceColor) {
                    moves += BoardMove.Simple(this, fromPos, Position.from(targetFile, targetRank))
                }
            }
        }
        return moves
    }

    protected fun lineAttacks(board: Board, directions: List<Pair<Int, Int>>): List<Position> {
        val attacks = mutableListOf<Position>()
        val fromPos = board.find(this) ?: return emptyList()
        directions.forEach { (df, dr) ->
            var i = 0
            while (true) {
                i++
                val targetFile = fromPos.file + df * i
                val targetRank = fromPos.rank + dr * i
                if (targetFile !in 1..8 || targetRank !in 1..8) break
                
                val toPos = Position.from(targetFile, targetRank)
                attacks += toPos
                
                if (board[targetFile, targetRank] != null) break
            }
        }
        return attacks
    }

    protected fun singleAttacks(board: Board, offsets: List<Pair<Int, Int>>): List<Position> {
        val attacks = mutableListOf<Position>()
        val fromPos = board.find(this) ?: return emptyList()
        offsets.forEach { (df, dr) ->
            val targetFile = fromPos.file + df
            val targetRank = fromPos.rank + dr
            if (targetFile in 1..8 && targetRank in 1..8) {
                attacks += Position.from(targetFile, targetRank)
            }
        }
        return attacks
    }

    // Overriding equals and hashCode to ensure equality by type and color
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Piece) return false
        if (this::class != other::class) return false
        if (pieceColor != other.pieceColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = pieceColor.hashCode()
        result = 31 * result + this::class.hashCode()
        return result
    }
}
