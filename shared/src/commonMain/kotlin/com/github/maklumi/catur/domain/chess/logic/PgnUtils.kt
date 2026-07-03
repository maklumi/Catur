package com.github.maklumi.catur.domain.chess.logic

import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.state.model.GameState
import com.github.maklumi.catur.state.model.GameStatus

object PgnUtils {
    fun generatePgn(state: GameState): String {
        val snapshot = state.currentSnapshot
        val pgn = StringBuilder()

        // Headers
        pgn.append("[Event \"${if (state.puzzle.currentPuzzleIndex != null) "Catur Puzzle" else "Casual Game"}\"]\n")
        pgn.append("[Site \"Android/Desktop\"]\n")
        pgn.append("[Date \"${getPlatform().getCurrentDate()}\"]\n")
        pgn.append("[White \"${state.match.whiteName}\"]\n")
        pgn.append("[Black \"${state.match.blackName}\"]\n")
        
        val result = when (snapshot.status) {
            GameStatus.CHECKMATE -> if (snapshot.activeColor == PieceColor.BLACK) "1-0" else "0-1"
            GameStatus.DRAW, GameStatus.STALEMATE -> "1/2-1/2"
            GameStatus.WHITE_RESIGNED, GameStatus.WHITE_OUT_OF_TIME -> "0-1"
            GameStatus.BLACK_RESIGNED, GameStatus.BLACK_OUT_OF_TIME -> "1-0"
            else -> "*"
        }
        pgn.append("[Result \"$result\"]\n\n")

        // Moves
        val historySnapshots = state.snapshots.drop(1)
        for (i in historySnapshots.indices) {
            if (i % 2 == 0) {
                pgn.append("${(i / 2) + 1}. ")
            }
            pgn.append("${historySnapshots[i].notation} ")
        }
        
        if (result != "*") {
            pgn.append(result)
        }

        return pgn.toString()
    }
}
