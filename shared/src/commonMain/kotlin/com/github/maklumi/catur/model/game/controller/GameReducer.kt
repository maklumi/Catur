package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*

fun gameReducer(state: GameState, action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareClick,
        is GameAction.PromotionChoice,
        is GameAction.EngineMove -> state.reduceMove(action)

        is GameAction.StepBack,
        is GameAction.StepForward,
        is GameAction.JumpToHistory -> state.reduceNavigation(action)

        is GameAction.Resign,
        is GameAction.OfferDraw,
        is GameAction.AcceptDraw,
        is GameAction.DeclineDraw,
        is GameAction.Tick -> state.reduceGameFlow(action)

        is GameAction.SquareLongPress,
        is GameAction.ClearLongPress,
        is GameAction.SetMoveEvaluations,
        is GameAction.SetBestMoveArrow,
        is GameAction.SetThreats,
        is GameAction.ReverseSides,
        is GameAction.SetEngineThinking,
        is GameAction.ChangeEngineLevel -> state.reduceUi(action)
    }
}
