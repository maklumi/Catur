package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.game.state.getNotation
import com.github.maklumi.catur.model.game.state.isInCheck
import com.github.maklumi.catur.model.piece.PieceColor

internal fun GameState.reduceMove(action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareClick -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.move(action.position)
            
            if (nextSnapshotBeforeNotation == currentSnapshot) return this

            if (nextSnapshotBeforeNotation.board != currentSnapshot.board) {
                val boardMove = nextSnapshotBeforeNotation.lastMove!!
                val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
                val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
                
                val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
                val nextSnapshot = nextSnapshotBeforeNotation.copy(
                    history = nextSnapshotBeforeNotation.history.copy(notation = notation)
                )

                val puzzle = puzzles.getOrNull(currentPuzzleIndex ?: -1)
                if (puzzle != null) {
                    val expectedMove = puzzle.solutionMoves.getOrNull(ui.currentPuzzleStep)
                    if (nextSnapshot.notation != expectedMove) {
                        // WRONG MOVE: Return current state without applying the move
                        return this
                    }
                    // CORRECT MOVE: Update the step and apply move
                    return applyIncrement()
                        .copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            ui = ui.copy(
                                currentPuzzleStep = ui.currentPuzzleStep + 1,
                                longPressedPosition = null,
                                moveEvaluations = emptyMap(),
                                bestMoveArrow = null,
                                threats = emptyList()
                            )
                        )
                }

                applyIncrement()
                    .copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        ui = ui.copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null, threats = emptyList())
                    )
            } else {
                copy(
                    snapshots = snapshots.toMutableList().apply { 
                        set(currentIndex, nextSnapshotBeforeNotation) 
                    },
                    ui = ui.copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null, threats = emptyList())
                )
            }
        }
        is GameAction.PromotionChoice -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.promote(action.move)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(action.move, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            val puzzle = puzzles.getOrNull(currentPuzzleIndex ?: -1)
            if (puzzle != null) {
                val expectedMove = puzzle.solutionMoves.getOrNull(ui.currentPuzzleStep)
                if (nextSnapshot.notation != expectedMove) return this
                return applyIncrement()
                    .copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        ui = ui.copy(currentPuzzleStep = ui.currentPuzzleStep + 1, bestMoveArrow = null, threats = emptyList())
                    )
            }

            applyIncrement()
                .copy(
                    snapshots = snapshots + nextSnapshot,
                    currentIndex = currentIndex + 1,
                    ui = ui.copy(bestMoveArrow = null, threats = emptyList())
                )
        }
        is GameAction.EngineMove -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val boardMove = currentSnapshot.findMoveByUci(action.moveUci) ?: return this
            
            val nextSnapshotBeforeNotation = currentSnapshot.promote(boardMove)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            val puzzle = puzzles.getOrNull(currentPuzzleIndex ?: -1)
            if (puzzle != null) {
                return applyIncrement()
                    .copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        ui = ui.copy(currentPuzzleStep = ui.currentPuzzleStep + 1, bestMoveArrow = null, threats = emptyList())
                    )
            }

            applyIncrement()
                .copy(
                    snapshots = snapshots + nextSnapshot,
                    currentIndex = currentIndex + 1,
                    ui = ui.copy(bestMoveArrow = null, threats = emptyList())
                )
        }
        else -> this
    }
}

private fun GameState.applyIncrement(): GameState {
    val justMovedColor = currentSnapshot.activeColor
    return if (justMovedColor == PieceColor.WHITE) {
        copy(whitePlayer = whitePlayer.copy(timeMillis = whiteTimeMillis + 3000L))
    } else {
        copy(blackPlayer = blackPlayer.copy(timeMillis = blackTimeMillis + 3000L))
    }
}
