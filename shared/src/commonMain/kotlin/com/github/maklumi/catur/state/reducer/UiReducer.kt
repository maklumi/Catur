package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.state.model.*

internal fun GameState.reduceUi(action: GameAction.Ui): GameState {
    return when (action) {
        is GameAction.Ui.SquareLongPress -> {
            updateVisual { copy(longPressedPosition = action.position) }
        }
        GameAction.Ui.ClearLongPress -> {
            updateVisual { copy(longPressedPosition = null, moveEvaluations = emptyMap(), threats = emptyList()) }
        }
        is GameAction.Ui.SetMoveEvaluations -> {
            updateVisual { copy(moveEvaluations = action.evaluations) }
        }
        is GameAction.Ui.SetBestMoveArrow -> {
            updateVisual { copy(bestMoveArrow = action.from to action.to) }
        }
        GameAction.Ui.ClearBestMoveArrow -> {
            updateVisual { copy(bestMoveArrow = null) }
        }
        is GameAction.Ui.SetThreats -> {
            updateVisual { copy(threats = action.threats) }
        }
        is GameAction.Ui.SetCurrentEvaluation -> {
            updateVisual { copy(currentEvaluation = action.evaluation) }
        }
        GameAction.Ui.ClearCurrentEvaluation -> {
            updateVisual { copy(currentEvaluation = null) }
        }
        is GameAction.Ui.SelectPalettePiece -> {
            updateVisual { copy(selectedPalettePiece = action.piece) }
        }
        GameAction.Ui.SelectEraser -> {
            updateVisual { copy(selectedPalettePiece = null) }
        }
        GameAction.Ui.ClearBoard -> {
            val newSnapshot = currentSnapshot.copy(
                context = currentSnapshot.context.copy(board = Board(emptyMap())),
                history = ChessHistory()
            )
            updateBoard {
                copy(
                    snapshots = listOf(newSnapshot),
                    currentIndex = 0
                )
            }
        }
        GameAction.Ui.ResetBoard -> {
            updateBoard {
                copy(
                    snapshots = listOf(GameSnapshotState(context = ChessContext(board = Board.initial))),
                    currentIndex = 0
                )
            }
        }
        is GameAction.Ui.SetEditMode -> {
            updateBoard { copy(isEditMode = action.enabled) }
        }
        is GameAction.Ui.SetEngineThinking -> {
            updateEngine { copy(isThinking = action.isThinking) }
        }
        is GameAction.Ui.ChangeEngineLevel -> {
            updateEngine { copy(model = action.model) }
        }
        is GameAction.Ui.SetBoardTheme -> {
            updateVisual { copy(boardTheme = action.theme) }
        }
        is GameAction.Ui.SetSoundEnabled -> {
            updateVisual { copy(isSoundEnabled = action.enabled) }
        }
    }
}
