package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.engine.ChessEngine
import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameController(
    private val engine: ChessEngine? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var lastEngineTurnIndex = -1

    init {
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
                        
                        val bestMove = engine.getBestMove(moves)

                        dispatch(GameAction.SetEngineThinking(false))
                        
                        if (bestMove != null) {
                            dispatch(GameAction.EngineMove(bestMove))
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
}
