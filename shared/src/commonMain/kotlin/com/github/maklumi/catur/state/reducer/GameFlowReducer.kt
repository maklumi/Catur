package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.domain.chess.piece.PieceColor

internal fun GameState.reduceGameFlow(action: GameAction.Flow): GameState {
    val platform = getPlatform()
    return when (action) {
        GameAction.Flow.Resign -> {
            if (board.isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            val forcedStatus = if (snapshot.activeColor == PieceColor.WHITE) GameStatus.WHITE_RESIGNED else GameStatus.BLACK_RESIGNED
            updateBoard { copy(snapshots = snapshots.toMutableList().apply { set(currentIndex, snapshot.copy(forcedStatus = forcedStatus)) }) }
        }
        GameAction.Flow.OfferDraw -> {
            if (board.isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy != null) return this
            updateBoard { copy(snapshots = snapshots.toMutableList().apply { set(currentIndex, snapshot.copy(drawOfferedBy = snapshot.activeColor)) }) }
        }
        GameAction.Flow.AcceptDraw -> {
            if (board.isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            updateBoard { copy(snapshots = snapshots.toMutableList().apply { set(currentIndex, snapshot.copy(forcedStatus = GameStatus.DRAW)) }) }
        }
        GameAction.Flow.DeclineDraw -> {
            if (board.isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            updateBoard { copy(snapshots = snapshots.toMutableList().apply { set(currentIndex, snapshot.copy(drawOfferedBy = null)) }) }
        }
        is GameAction.Flow.Tick -> {
            val justMovedColor = currentSnapshot.activeColor
            if (justMovedColor == PieceColor.WHITE) {
                updateClock { copy(whiteTimeMillis = (whiteTimeMillis - action.millis).coerceAtLeast(0)) }
            } else {
                updateClock { copy(blackTimeMillis = (blackTimeMillis - action.millis).coerceAtLeast(0)) }
            }
        }
        GameAction.Flow.ReverseSides -> {
            updateMatch {
                copy(
                    whiteName = blackName,
                    whiteType = blackType,
                    blackName = whiteName,
                    blackType = whiteType
                )
            }.updateBoard { copy(isBoardFlipped = !isBoardFlipped) }
        }
        GameAction.Flow.NewGame -> {
            copy(
                board = BoardState(),
                match = MatchState(id = platform.generateId()),
                clock = ClockState(),
                engine = engine.copy(isThinking = false),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(currentScreen = Screen.GAME, currentEvaluation = null, bestMoveArrow = null)
            )
        }
        GameAction.Flow.StartLocalGame -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    id = platform.generateId(),
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
        is GameAction.Flow.StartComputerGame -> {
            val isHumanWhite = action.playerColor == PieceColor.WHITE
            copy(
                board = BoardState(isBoardFlipped = !isHumanWhite),
                match = MatchState(
                    id = platform.generateId(),
                    whiteName = if (isHumanWhite) "Human" else "Computer (${action.model})",
                    whiteType = if (isHumanWhite) PlayerType.HUMAN else PlayerType.ENGINE,
                    blackName = if (isHumanWhite) "Computer (${action.model})" else "Human",
                    blackType = if (isHumanWhite) PlayerType.ENGINE else PlayerType.HUMAN
                ),
                clock = ClockState(),
                engine = engine.copy(isThinking = false, model = action.model),
                puzzle = puzzle.copy(currentPuzzleIndex = null, currentPuzzleStep = 0),
                uiVisual = uiVisual.copy(
                    currentScreen = Screen.GAME,
                    currentEvaluation = null,
                    bestMoveArrow = null
                )
            )
        }
        GameAction.Flow.StartAnalysis -> {
            copy(
                board = BoardState(),
                match = MatchState(
                    id = platform.generateId(),
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
    }
}
