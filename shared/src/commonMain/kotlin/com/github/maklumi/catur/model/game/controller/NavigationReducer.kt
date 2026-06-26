package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

internal fun GameState.reduceNavigation(action: GameAction): GameState {
    return when (action) {
        is GameAction.StepBack -> {
            if (canGoBack()) copy(currentIndex = currentIndex - 1, ui = ui.copy(bestMoveArrow = null, threats = emptyList())) else this
        }
        is GameAction.StepForward -> {
            if (canGoForward()) copy(currentIndex = currentIndex + 1, ui = ui.copy(bestMoveArrow = null, threats = emptyList())) else this
        }
        is GameAction.JumpToHistory -> {
            if (action.index in snapshots.indices) {
                copy(currentIndex = action.index, ui = ui.copy(bestMoveArrow = null, threats = emptyList()))
            } else this
        }
        else -> this
    }
}
