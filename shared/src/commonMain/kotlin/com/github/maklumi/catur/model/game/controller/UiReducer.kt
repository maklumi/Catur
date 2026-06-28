package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor

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
        is GameAction.SetPuzzles -> {
            copy(ui = ui.copy(puzzles = action.puzzles))
        }
        is GameAction.PuzzleCompleted -> {
            val updatedPuzzles = ui.puzzles.mapIndexed { index, puzzle ->
                if (index == action.index) puzzle.copy(isCompleted = true) else puzzle
            }
            copy(ui = ui.copy(
                puzzles = updatedPuzzles,
                completedPuzzleIndices = ui.completedPuzzleIndices + action.index
            ))
        }
        is GameAction.SelectPuzzle -> {
            val puzzle = ui.puzzles.getOrNull(action.index) ?: return this
            val board = Board.fromFen(puzzle.initialFen)
            val activeColor = Board.parseActiveColor(puzzle.initialFen)

            // 1. Parse names: "White vs Black, Venue, Date"
            val namesPart = puzzle.title.split(",").firstOrNull() ?: ""
            val names = namesPart.split(" vs ")
            val whiteName = names.getOrNull(0)?.trim() ?: "White"
            val blackName = names.getOrNull(1)?.trim() ?: "Black"

            copy(
                snapshots = listOf(GameSnapshotState(context = ChessContext(board = board, activeColor = activeColor))),
                currentIndex = 0,
                whitePlayer = whitePlayer.copy(name = whiteName, timeMillis = 600_000L),
                blackPlayer = blackPlayer.copy(name = blackName, timeMillis = 600_000L),
                engine = engine.copy(isThinking = false),
                ui = ui.copy(
                    currentPuzzleIndex = action.index,
                    currentPuzzleStep = 0,
                    bestMoveArrow = null,
                    threats = emptyList(),
                    moveEvaluations = emptyMap(),
                    isBoardFlipped = activeColor == PieceColor.BLACK)
            )
        }
        else -> this
    }
}
