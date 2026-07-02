package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

fun gameReducer(state: GameState, action: GameAction): GameState {
    return when (action) {
        is GameAction.Move -> state.reduceMove(action)
        is GameAction.Nav -> state.reduceNavigation(action)
        is GameAction.Flow -> state.reduceGameFlow(action)
        is GameAction.Ui -> state.reduceUi(action)
        is GameAction.Puzzles -> state.reducePuzzles(action)
    }
}
