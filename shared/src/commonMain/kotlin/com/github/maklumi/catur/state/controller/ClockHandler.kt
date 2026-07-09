package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.GameStatus
import com.github.maklumi.catur.state.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ClockHandler(private val dispatch: (GameAction) -> Unit) {
    fun attach(scope: CoroutineScope, state: StateFlow<GameState>) {
        scope.launch {
            while (true) {
                delay(100.milliseconds)
                val currentState = state.value
                if (!currentState.board.isViewingHistory && 
                    currentState.currentSnapshot.status == GameStatus.ONGOING &&
                    currentState.puzzle.currentPuzzleIndex == null
                ) {
                    dispatch(GameAction.Flow.Tick(100))
                }
            }
        }
    }
}
