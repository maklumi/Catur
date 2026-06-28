package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

internal fun GameState.reduceNavigation(action: GameAction): GameState {
    return when (action) {
        is GameAction.StepBack -> {
            if (canGoBack()) {
                copy(
                    board = board.copy(currentIndex = board.currentIndex - 1),
                    uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList())
                )
            } else this
        }
        is GameAction.StepForward -> {
            if (canGoForward()) {
                copy(
                    board = board.copy(currentIndex = board.currentIndex + 1),
                    uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList())
                )
            } else this
        }
        is GameAction.JumpToHistory -> {
            if (action.index in board.snapshots.indices) {
                copy(
                    board = board.copy(currentIndex = action.index),
                    uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList())
                )
            } else this
        }
        else -> this
    }
}
