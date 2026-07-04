package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.state.model.*

internal fun GameState.reduceHistory(action: GameAction.History): GameState {
    return when (action) {
        is GameAction.History.SetPastGames -> {
            updateHistory { copy(pastGames = action.games) }
        }
        is GameAction.History.LoadGame -> {
            // Loading from PGN is complex because we need to re-play all moves
            // For now, we'll just navigate to analysis mode
            // Future: Implement PGN replayer in GameController
            updateVisual { copy(currentScreen = Screen.ANALYSIS) }
        }
    }
}
