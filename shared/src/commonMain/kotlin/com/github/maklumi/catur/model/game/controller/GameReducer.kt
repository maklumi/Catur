package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

fun gameReducer(state: GameState, action: GameAction): GameState {
    val nextState = when (action) {
        is GameAction.Move -> state.reduceMove(action)
        is GameAction.Nav -> state.reduceNavigation(action)
        is GameAction.Flow -> state.reduceGameFlow(action)
        is GameAction.Ui -> state.reduceUi(action)
        is GameAction.Puzzles -> state.reducePuzzles(action)
    }

    return if (nextState.board.snapshots != state.board.snapshots || nextState.board.currentIndex != state.board.currentIndex) {
        nextState.updateBoard { copy(openingName = nextState.identifyOpening()) }
    } else {
        nextState
    }
}
