package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.domain.chess.notation.findMoveByNotation
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PuzzleHandler(private val dispatch: (GameAction) -> Unit) {
    fun attach(scope: CoroutineScope, state: StateFlow<GameState>) {
        // Automatic Opponent Moves
        scope.launch {
            state.map { it.puzzle.currentPuzzleStep }.distinctUntilChanged().collect { step ->
                val puzzle = state.value.puzzle.puzzles.getOrNull(state.value.puzzle.currentPuzzleIndex ?: -1)
                // If it's an odd step (0 = user, 1 = opponent, 2 = user...), play automatically
                if (puzzle != null && step % 2 != 0) {
                    val nextNotation = puzzle.solutionMoves.getOrNull(step)
                    if (nextNotation != null) {
                        delay(1000.milliseconds) // Wait for realism
                        val snapshot = state.value.currentSnapshot
                        val boardMove = snapshot.board.findMoveByNotation(
                            nextNotation,
                            snapshot.activeColor,
                            snapshot.lastMove,
                            snapshot.movedPositions
                        )
                        if (boardMove != null) {
                            dispatch(GameAction.Move.EngineMove(boardMove.toUciString()))
                        }
                    }
                }
            }
        }

        // Auto-forward puzzle logic
        scope.launch {
            state.map { it.puzzle.isPuzzleFinished }.distinctUntilChanged().collect { isFinished ->
                if (isFinished && state.value.puzzle.isAutoForward) {
                    delay(3000.milliseconds)
                    dispatch(GameAction.Puzzles.NextPuzzle)
                }
            }
        }
    }
}
