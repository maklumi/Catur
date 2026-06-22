package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.model.game.audio.SoundType
import com.github.maklumi.catur.model.game.engine.ChessEngine
import com.github.maklumi.catur.model.game.engine.StockfishChessEngine
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
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
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val stockfishChessEngine = StockfishChessEngine()

    private var lastEngineTurnIndex = -1

    init {
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
            state.collect { currentState ->
                val currentSnapshotIndex = currentState.snapshots.size
                if (currentState.isEngineTurn && 
                    engine != null && 
                    !currentState.isEngineThinking && 
                    currentSnapshotIndex != lastEngineTurnIndex
                ) {
                    lastEngineTurnIndex = currentSnapshotIndex
                    
                    launch {
                        dispatch(GameAction.SetEngineThinking(true))
                        
                        val moves = currentState.snapshots
                            .drop(1)
                            .mapNotNull { it.lastMoveUci }
                        
                        val bestMove = engine.getBestMove(moves, currentState.engineModel)

                        dispatch(GameAction.SetEngineThinking(false))
                        
                        if (bestMove != null) {
                            dispatch(GameAction.EngineMove(bestMove))
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
                        val score = stockfishChessEngine.evaluate(currentMoves + uci)
                        evals[uci] = score
                        dispatch(GameAction.SetMoveEvaluations(evals.toMap()))
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
        stockfishChessEngine.stop()
    }
}
