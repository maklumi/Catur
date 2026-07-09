package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.domain.engine.ChessEngine
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.state.reducer.gameReducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class GameController(
    private val engine: ChessEngine? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    platform: Platform = getPlatform()
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val boardState = _state.map { it.board }.distinctUntilChanged()
    val matchState = _state.map { it.match }.distinctUntilChanged()
    val clockState = _state.map { it.clock }.distinctUntilChanged()
    val engineState = _state.map { it.engine }.distinctUntilChanged()
    val puzzleState = _state.map { it.puzzle }.distinctUntilChanged()
    val uiVisualState = _state.map { it.uiVisual }.distinctUntilChanged()
    val historyState = _state.map { it.history }.distinctUntilChanged()

    init {
        // Handlers
        SoundHandler(platform).attach(scope, state)
        ClockHandler(::dispatch).attach(scope, state)
        PersistenceHandler(platform, ::dispatch).attach(scope, state)
        EngineHandler(engine, ::dispatch).attach(scope, state)
        PuzzleHandler(::dispatch).attach(scope, state)
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
