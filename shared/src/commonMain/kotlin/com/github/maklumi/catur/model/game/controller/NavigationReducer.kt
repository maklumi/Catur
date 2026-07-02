package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

internal fun GameState.reduceNavigation(action: GameAction.Nav): GameState {
    return when (action) {
        GameAction.Nav.StepBack -> {
            if (canGoBack()) {
                updateBoard { copy(currentIndex = currentIndex - 1) }
                    .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
            } else this
        }
        GameAction.Nav.StepForward -> {
            if (canGoForward()) {
                updateBoard { copy(currentIndex = currentIndex + 1) }
                    .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
            } else this
        }
        is GameAction.Nav.JumpToHistory -> {
            if (action.index in board.snapshots.indices) {
                updateBoard { copy(currentIndex = action.index) }
                    .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
            } else this
        }
        is GameAction.Nav.NavigateTo -> {
            updateVisual { copy(currentScreen = action.screen) }
        }
    }
}
