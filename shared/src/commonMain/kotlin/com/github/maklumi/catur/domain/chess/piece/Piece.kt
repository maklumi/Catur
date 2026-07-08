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
        val square = board.find(this) ?: return emptyList()
        directions.forEach { (df, dr) ->
            var i = 0
            while (true) {
                i++
                val target = board[square.file + df * i, square.rank + dr * i] ?: break
                if (target.hasPiece(pieceColor)) break
                moves += BoardMove.Simple(this, square.position, target.position)
                if (target.isNotEmpty) break
            }
        }
        return moves
    }

    protected fun singleMoves(board: Board, offsets: List<Pair<Int, Int>>): List<BoardMove> {
        val moves = mutableListOf<BoardMove>()
        val square = board.find(this) ?: return emptyList()
        offsets.forEach { (df, dr) ->
            val target = board[square.file + df, square.rank + dr] ?: return@forEach
            if (!target.hasPiece(pieceColor)) {
                moves += BoardMove.Simple(this, square.position, target.position)
            }
        }
        return moves
    }

    protected fun lineAttacks(board: Board, directions: List<Pair<Int, Int>>): List<Position> {
        val attacks = mutableListOf<Position>()
        val square = board.find(this) ?: return emptyList()
        directions.forEach { (df, dr) ->
            var i = 0
            while (true) {
                i++
                val target = board[square.file + df * i, square.rank + dr * i] ?: break
                attacks += target.position
                if (target.isNotEmpty) break
            }
        }
        return attacks
    }

    protected fun singleAttacks(board: Board, offsets: List<Pair<Int, Int>>): List<Position> {
        val attacks = mutableListOf<Position>()
        val square = board.find(this) ?: return emptyList()
        offsets.forEach { (df, dr) ->
            val target = board[square.file + df, square.rank + dr] ?: return@forEach
            attacks += target.position
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
