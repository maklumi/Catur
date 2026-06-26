package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

internal fun GameState.reduceUi(action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareLongPress -> {
            copy(ui = ui.copy(longPressedPosition = action.position))
        }
        is GameAction.ClearLongPress -> {
            copy(ui = ui.copy(longPressedPosition = null, moveEvaluations = emptyMap(), threats = emptyList()))
        }
        is GameAction.SetMoveEvaluations -> {
            copy(ui = ui.copy(moveEvaluations = action.evaluations))
        }
        is GameAction.SetBestMoveArrow -> {
            val arrow = if (action.from != null && action.to != null) action.from to action.to else null
            copy(ui = ui.copy(bestMoveArrow = arrow))
        }
        is GameAction.SetThreats -> {
            copy(ui = ui.copy(threats = action.threats))
        }
        is GameAction.ReverseSides -> {
            copy(
                whitePlayer = blackPlayer,
                blackPlayer = whitePlayer,
                ui = ui.copy(isBoardFlipped = !isBoardFlipped)
            )
        }
        is GameAction.SetEngineThinking -> {
            copy(engine = engine.copy(isThinking = action.isThinking))
        }
        is GameAction.ChangeEngineLevel -> {
            copy(engine = engine.copy(model = action.model))
        }
        else -> this
    }
}
