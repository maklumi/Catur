package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.engine.ChessEngine
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.GameState
import com.github.maklumi.catur.state.model.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class EngineHandler(
    private val engine: ChessEngine?,
    private val dispatch: (GameAction) -> Unit
) {
    fun attach(scope: CoroutineScope, state: StateFlow<GameState>) {
        if (engine == null) return

        // Engine Turn & Background Analysis
        scope.launch {
            state
                .map { Triple(it.board, it.board.currentIndex, it.isEngineTurn) }
                .distinctUntilChanged()
                .collectLatest { (boardState, currentIndex, isEngineTurn) ->
                    val currentState = state.value
                    if (isEngineTurn && !currentState.engine.isThinking) {
                        dispatch(GameAction.Ui.SetEngineThinking(isThinking = true))

                        val moves = boardState.snapshots
                            .drop(1)
                            .mapNotNull { it.lastMoveUci }

                        val bestMove = engine.getBestMove(moves, currentState.engine.model)

                        dispatch(GameAction.Ui.SetEngineThinking(false))

                        if (bestMove != null) {
                            delay(1000.milliseconds)
                            dispatch(GameAction.Move.EngineMove(bestMove))
                        }
                    } else if (!isEngineTurn && !currentState.engine.isThinking) {
                        if (currentState.puzzle.currentPuzzleIndex == null && !currentState.board.isEditMode) {
                            val moves = currentState.snapshots
                                .take(currentIndex + 1)
                                .asSequence()
                                .drop(1)
                                .mapNotNull { it.lastMoveUci }
                                .toList()

                            val isStandardStart = currentState.isFromInitialPosition()
                            val engineFen = if (isStandardStart) null else boardState.snapshots[0].generateFen()

                            // Calculate Threats
                            val snapshot = currentState.currentSnapshot
                            val activeColor = snapshot.activeColor
                            val opponentColor = activeColor.opposite()
                            val threats = snapshot.board.piecesMap.keys.filter { pos ->
                                val piece = snapshot.board[pos]
                                piece?.pieceColor == activeColor && snapshot.board.isAttacked(pos, opponentColor)
                            }
                            dispatch(GameAction.Ui.SetThreats(threats))

                            // Best move arrow
                            val bestMove = engine.getBestMove(moves, "stockfish", engineFen)
                            if (bestMove != null && bestMove.length >= 4) {
                                try {
                                    val from = Position.valueOf(bestMove.substring(0, 2))
                                    val to = Position.valueOf(bestMove.substring(2, 4))
                                    dispatch(GameAction.Ui.SetBestMoveArrow(from, to))
                                } catch (_: Exception) {}
                            }

                            // Current Position Evaluation
                            val eval = engine.evaluate(moves, "stockfish", engineFen)
                            dispatch(GameAction.Ui.SetCurrentEvaluation(eval))

                            // Top 3 Moves for Analysis
                            if (currentState.uiVisual.currentScreen == Screen.ANALYSIS) {
                                val topMoves = engine.getTopMoves(moves, "stockfish", 3, engineFen)
                                val mapped = topMoves.mapNotNull { (uci, score) ->
                                    try {
                                        val from = Position.valueOf(uci.substring(0, 2))
                                        val to = Position.valueOf(uci.substring(2, 4))
                                        (from to to) to score
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                                dispatch(GameAction.Ui.SetTopAnalysisMoves(mapped))
                            } else {
                                if (currentState.uiVisual.topAnalysisMoves.isNotEmpty()) {
                                    dispatch(GameAction.Ui.SetTopAnalysisMoves(emptyList()))
                                }
                            }
                        }
                    }
                }
        }

        // Long Press Move Evaluations
        scope.launch {
            state.map { it.longPressedPosition }.distinctUntilChanged().collectLatest { pos ->
                dispatch(GameAction.Ui.SetMoveEvaluations(emptyMap()))
                if (pos != null) {
                    val currentMoves = state.value.snapshots.take(state.value.board.currentIndex + 1).drop(1).mapNotNull { it.lastMoveUci }
                    val legalMoves = state.value.currentSnapshot.getLegalMovesForPosition(pos)
                    val evals = mutableMapOf<String, Int>()

                    for (boardMove in legalMoves) {
                        val uci = boardMove.toUciString()
                        val score = engine.evaluate(currentMoves + uci, "stockfish")
                        evals[uci] = score
                        dispatch(GameAction.Ui.SetMoveEvaluations(evals.toMap()))
                    }
                }
            }
        }
    }
}
