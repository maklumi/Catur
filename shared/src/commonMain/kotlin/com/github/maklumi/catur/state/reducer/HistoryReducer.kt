package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.isInCheck
import com.github.maklumi.catur.domain.chess.notation.PgnUtils
import com.github.maklumi.catur.domain.chess.notation.findMoveByNotation
import com.github.maklumi.catur.domain.chess.notation.getNotation
import com.github.maklumi.catur.state.model.*

internal fun GameState.reduceHistory(action: GameAction.History): GameState {
    return when (action) {
        is GameAction.History.SetPastGames -> {
            updateHistory { copy(pastGames = action.games) }
        }
        is GameAction.History.LoadGame -> {
            val fen = PgnUtils.extractFen(action.pgn) ?: Board.initial.toFen()
            val moves = PgnUtils.parseMoves(action.pgn)
            
            var currentSnapshot = GameSnapshotState(
                context = ChessContext(
                    board = Board.fromFen(fen),
                    activeColor = Board.parseActiveColor(fen)
                )
            )
            
            val snapshots = mutableListOf(currentSnapshot)
            
            for (notation in moves) {
                val boardMove = currentSnapshot.board.findMoveByNotation(
                    notation,
                    currentSnapshot.activeColor,
                    currentSnapshot.lastMove,
                    currentSnapshot.movedPositions
                ) ?: break
                
                val nextSnapshotBeforeNotation = currentSnapshot.promote(boardMove)
                val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
                val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
                
                val actualNotation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
                currentSnapshot = nextSnapshotBeforeNotation.copy(
                    history = nextSnapshotBeforeNotation.history.copy(notation = actualNotation)
                )
                snapshots.add(currentSnapshot)
            }
            
            copy(
                board = BoardState(
                    snapshots = snapshots,
                    currentIndex = snapshots.size - 1,
                    isBoardFlipped = false
                ),
                uiVisual = uiVisual.copy(currentScreen = Screen.ANALYSIS, isPgnImportDialogOpen = false),
                puzzle = PuzzleState() // Clear puzzle state when loading a game
            )
        }
    }
}
