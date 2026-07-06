package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.state.model.*

internal fun GameState.reducePuzzles(action: GameAction.Puzzles): GameState {
    return when (action) {
        is GameAction.Puzzles.SetPuzzles -> {
            updatePuzzle { 
                copy(
                    puzzles = action.puzzles,
                    completedPuzzleIndices = action.completedIndices
                ) 
            }
        }
        is GameAction.Puzzles.PuzzleCompleted -> {
            val updatedPuzzles = puzzle.puzzles.mapIndexed { index, p ->
                if (index == action.index) p.copy(isCompleted = true) else p
            }
            updatePuzzle {
                copy(
                    puzzles = updatedPuzzles,
                    completedPuzzleIndices = completedPuzzleIndices + action.index,
                    isPuzzleFinished = true
                )
            }
        }
        is GameAction.Puzzles.SelectPuzzle -> {
            val puzzleRef = puzzle.puzzles.getOrNull(action.index) ?: return this
            val boardInit = Board.fromFen(puzzleRef.initialFen)
            val activeColor = Board.parseActiveColor(puzzleRef.initialFen)

            val namesPart = puzzleRef.title.split(",").firstOrNull() ?: ""
            val names = namesPart.split(" vs ")
            val whiteName = names.getOrNull(0)?.trim() ?: "White"
            val blackName = names.getOrNull(1)?.trim() ?: "Black"

            val isWhiteHuman = activeColor == PieceColor.WHITE

            copy(
                board = BoardState(
                    snapshots = listOf(GameSnapshotState(context = ChessContext(board = boardInit, activeColor = activeColor))),
                    currentIndex = 0,
                    isBoardFlipped = activeColor == PieceColor.BLACK
                ),
                match = MatchState(
                    whiteName = whiteName,
                    whiteType = if (isWhiteHuman) PlayerType.HUMAN else PlayerType.ENGINE,
                    blackName = blackName,
                    blackType = if (isWhiteHuman) PlayerType.ENGINE else PlayerType.HUMAN
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(
                    currentPuzzleIndex = action.index,
                    currentPuzzleStep = 0,
                    isPuzzleFinished = false
                ),
                uiVisual = UiVisualState(currentScreen = Screen.GAME, boardTheme = uiVisual.boardTheme, isSoundEnabled = uiVisual.isSoundEnabled)
            )
        }
        is GameAction.Puzzles.NextPuzzle -> {
            val currentIndex = puzzle.currentPuzzleIndex ?: return this
            val nextIndex = (currentIndex + 1) % puzzle.puzzles.size
            reducePuzzles(GameAction.Puzzles.SelectPuzzle(nextIndex))
        }
        is GameAction.Puzzles.SetAutoForward -> {
            updatePuzzle { copy(isAutoForward = action.enabled) }
        }
    }
}
