package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.engine.ChessEngine
import com.github.maklumi.catur.model.game.puzzle.PuzzleLoader
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.game.state.findMoveByNotation
import com.github.maklumi.catur.model.game.state.isInCheck
import com.github.maklumi.catur.model.move.EnPassantMove
import com.github.maklumi.catur.model.move.toUciString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class GameController(
    private val engine: ChessEngine? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val platform: Platform = getPlatform()
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val boardState = _state.map { it.board }.distinctUntilChanged()
    val matchState = _state.map { it.match }.distinctUntilChanged()
    val clockState = _state.map { it.clock }.distinctUntilChanged()
    val engineState = _state.map { it.engine }.distinctUntilChanged()
    val puzzleState = _state.map { it.puzzle }.distinctUntilChanged()
    val uiVisualState = _state.map { it.uiVisual }.distinctUntilChanged()

    init {
        // Load puzzles
        scope.launch {
            val completed = platform.persistenceManager.loadCompletedPuzzles()
            val puzzles = PuzzleLoader.loadPuzzles(completed)
            if (puzzles.isNotEmpty()) {
                dispatch(GameAction.SetPuzzles(puzzles))
            }
        }

        // Save completed puzzles when they change
        scope.launch {
            state.collect { currentState ->
                platform.persistenceManager.saveCompletedPuzzles(currentState.completedPuzzleIndices)
            }
        }

        // Clock timer logic
        scope.launch {
            while (true) {
                delay(100.milliseconds)
                val currentState = state.value
                if (!currentState.isViewingHistory && currentState.currentSnapshot.status == GameStatus.ONGOING) {
                    dispatch(GameAction.Tick(100))
                }
            }
        }

        // Sound effects logic
        scope.launch {
            state
                .map { it.snapshots.size }
                .distinctUntilChanged()
                .drop(1) // Skip initial state
                .collect {
                    val currentState = state.value
                    if (currentState.snapshots.size < 2) return@collect
                    val lastSnapshot = currentState.snapshots.last()
                    val prevSnapshot = currentState.snapshots[currentState.snapshots.size - 2]

                    val move = lastSnapshot.lastMove?.move
                    if (move != null) {
                        val platform = getPlatform()
                        when {
                            lastSnapshot.status == GameStatus.CHECKMATE ||
                                    lastSnapshot.status == GameStatus.STALEMATE -> {
                                platform.playSound(SoundType.GAME_END)
                            }

                            lastSnapshot.board.isInCheck(lastSnapshot.activeColor) -> {
                                platform.playSound(SoundType.CHECK)
                            }

                            prevSnapshot.board[move.to].isNotEmpty || move is EnPassantMove -> {
                                platform.playSound(SoundType.CAPTURE)
                            }

                            else -> {
                                platform.playSound(SoundType.MOVE)
                            }
                        }
                    }
                }
        }

        scope.launch {
            state
                .map { it.snapshots.size to it.isEngineTurn }
                .distinctUntilChanged()
                .collectLatest { (snapshotSize, isEngineTurn) ->
                    val currentState = state.value
                    if (isEngineTurn && engine != null && !currentState.isEngineThinking) {
                        dispatch(GameAction.SetEngineThinking(true))

                        val moves = currentState.snapshots
                            .drop(1)
                            .mapNotNull { it.lastMoveUci }

                        val bestMove = engine.getBestMove(moves, currentState.engineModel)

                        dispatch(GameAction.SetEngineThinking(false))

                        if (bestMove != null) {
                            delay(1000.milliseconds)
                            dispatch(GameAction.EngineMove(bestMove))
                        }
                    } else if (!isEngineTurn && engine != null && !currentState.isEngineThinking) {
                        // Background analysis for best move arrow and threats (Disabled during puzzles)
                        if (currentState.currentPuzzleIndex == null) {
                            // Calculate Threats
                            val snapshot = currentState.currentSnapshot
                            val activeColor = snapshot.activeColor
                            val opponentColor = activeColor.opposite()
                            val threats = snapshot.board.piecesMap.keys.filter { pos ->
                                val piece = snapshot.board[pos].piece
                                piece?.pieceColor == activeColor && snapshot.board.isAttacked(
                                    pos,
                                    opponentColor
                                )
                            }
                            dispatch(GameAction.SetThreats(threats))

                            // Best move arrow
                            val moves = currentState.snapshots
                                .drop(1)
                                .mapNotNull { it.lastMoveUci }

                            val bestMove = engine.getBestMove(moves, currentState.engineModel)
                            if (bestMove != null && bestMove.length >= 4) {
                                try {
                                    val from = Position.valueOf(bestMove.substring(0, 2))
                                    val to = Position.valueOf(bestMove.substring(2, 4))
                                    dispatch(GameAction.SetBestMoveArrow(from, to))
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                }
        }

        scope.launch {
            state.map { it.longPressedPosition }.distinctUntilChanged().collectLatest { pos ->
                dispatch(GameAction.SetMoveEvaluations(emptyMap()))
                if (pos != null) {
                    val currentMoves = state.value.snapshots.drop(1).mapNotNull { it.lastMoveUci }
                    val legalMoves = state.value.currentSnapshot.getLegalMovesForPosition(pos)
                    val evals = mutableMapOf<String, Int>()

                    for (boardMove in legalMoves) {
                        val uci = boardMove.move.toUciString()
                        val score = engine?.evaluate(currentMoves + uci)
                        evals[uci] = score ?: 0
                        dispatch(GameAction.SetMoveEvaluations(evals.toMap()))
                    }
                }
            }
        }

        scope.launch {
            state.map { it.puzzle.currentPuzzleStep }.distinctUntilChanged().collect { step ->
                val puzzle = state.value.puzzles.getOrNull(state.value.currentPuzzleIndex ?: -1)
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
                            dispatch(GameAction.EngineMove(boardMove.move.toUciString()))
                        }
                    }
                }
            }
        }
    }

    fun dispatch(action: GameAction) {
        _state.update { currentState ->
            gameReducer(currentState, action)
        }
    }

    fun dispose() {
        engine?.stop()
    }
}
