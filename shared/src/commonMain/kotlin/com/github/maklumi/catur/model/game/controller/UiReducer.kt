package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor

internal fun GameState.reduceUi(action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareLongPress -> {
            copy(uiVisual = uiVisual.copy(longPressedPosition = action.position))
        }
        is GameAction.ClearLongPress -> {
            copy(uiVisual = uiVisual.copy(longPressedPosition = null, moveEvaluations = emptyMap(), threats = emptyList()))
        }
        is GameAction.SetMoveEvaluations -> {
            copy(uiVisual = uiVisual.copy(moveEvaluations = action.evaluations))
        }
        is GameAction.SetBestMoveArrow -> {
            val arrow = if (action.from != null && action.to != null) action.from to action.to else null
            copy(uiVisual = uiVisual.copy(bestMoveArrow = arrow))
        }
        is GameAction.SetThreats -> {
            copy(uiVisual = uiVisual.copy(threats = action.threats))
        }
        is GameAction.ReverseSides -> {
            copy(
                match = match.copy(
                    whiteName = match.blackName,
                    whiteType = match.blackType,
                    blackName = match.whiteName,
                    blackType = match.whiteType
                ),
                board = board.copy(isBoardFlipped = !isBoardFlipped)
            )
        }
        is GameAction.SetEngineThinking -> {
            copy(engine = engine.copy(isThinking = action.isThinking))
        }
        is GameAction.ChangeEngineLevel -> {
            copy(engine = engine.copy(model = action.model))
        }
        is GameAction.SetPuzzles -> {
            copy(puzzle = puzzle.copy(puzzles = action.puzzles))
        }
        is GameAction.PuzzleCompleted -> {
            val updatedPuzzles = puzzle.puzzles.mapIndexed { index, p ->
                if (index == action.index) p.copy(isCompleted = true) else p
            }
            copy(puzzle = puzzle.copy(
                puzzles = updatedPuzzles,
                completedPuzzleIndices = puzzle.completedPuzzleIndices + action.index
            ))
        }
        is GameAction.SetCurrentEvaluation -> {
            copy(uiVisual = uiVisual.copy(currentEvaluation = action.evaluation))
        }
        is GameAction.NavigateTo -> {
            copy(uiVisual = uiVisual.copy(currentScreen = action.screen))
        }
        is GameAction.NewGame -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    whiteName = "Human",
                    whiteType = PlayerType.HUMAN,
                    blackName = "Maia",
                    blackType = PlayerType.ENGINE
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(currentScreen = Screen.GAME, currentEvaluation = null, bestMoveArrow = null)
            )
        }
        is GameAction.StartLocalGame -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    whiteName = "White",
                    whiteType = PlayerType.HUMAN,
                    blackName = "Black",
                    blackType = PlayerType.HUMAN
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(currentScreen = Screen.GAME, currentEvaluation = null, bestMoveArrow = null)
            )
        }
        is GameAction.StartComputerGame -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    whiteName = "Human",
                    whiteType = PlayerType.HUMAN,
                    blackName = "Maia",
                    blackType = PlayerType.ENGINE
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(currentScreen = Screen.GAME, currentEvaluation = null, bestMoveArrow = null)
            )
        }
        is GameAction.StartAnalysis -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    whiteName = "White",
                    whiteType = PlayerType.HUMAN,
                    blackName = "Black",
                    blackType = PlayerType.HUMAN
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(currentScreen = Screen.ANALYSIS, currentEvaluation = null, bestMoveArrow = null)
            )
        }
        is GameAction.SetEditMode -> {
            copy(board = board.copy(isEditMode = action.enabled))
        }
        is GameAction.SelectPalettePiece -> {
            copy(uiVisual = uiVisual.copy(selectedPalettePiece = action.piece))
        }
        is GameAction.ClearBoard -> {
            val newSnapshot = currentSnapshot.copy(
                context = currentSnapshot.context.copy(board = Board(emptyMap())),
                history = ChessHistory()
            )
            copy(
                board = board.copy(
                    snapshots = listOf(newSnapshot),
                    currentIndex = 0
                )
            )
        }
        is GameAction.ResetBoard -> {
            copy(
                board = board.copy(
                    snapshots = listOf(GameSnapshotState(context = ChessContext(board = Board.initial))),
                    currentIndex = 0
                )
            )
        }
        is GameAction.SelectPuzzle -> {
            val puzzleRef = puzzle.puzzles.getOrNull(action.index) ?: return this
            val boardInit = Board.fromFen(puzzleRef.initialFen)
            val activeColor = Board.parseActiveColor(puzzleRef.initialFen)

            // 1. Parse names: "White vs Black, Venue, Date"
            val namesPart = puzzleRef.title.split(",").firstOrNull() ?: ""
            val names = namesPart.split(" vs ")
            val whiteName = names.getOrNull(0)?.trim() ?: "White"
            val blackName = names.getOrNull(1)?.trim() ?: "Black"

            copy(
                board = BoardState(
                    snapshots = listOf(GameSnapshotState(context = ChessContext(board = boardInit, activeColor = activeColor))),
                    currentIndex = 0,
                    isBoardFlipped = activeColor == PieceColor.BLACK
                ),
                match = MatchState(
                    whiteName = whiteName,
                    whiteType = PlayerType.HUMAN,
                    blackName = blackName,
                    blackType = PlayerType.ENGINE
                ),
                clock = ClockState(
                    whiteTimeMillis = 600_000L,
                    blackTimeMillis = 600_000L
                ),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(
                    currentPuzzleIndex = action.index,
                    currentPuzzleStep = 0
                ),
                uiVisual = uiVisual.copy(currentScreen = Screen.GAME)
            )
        }
        else -> this
    }
}
