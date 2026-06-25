package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor

fun gameReducer(state: GameState, action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareClick -> {
            if (state.isViewingHistory) return state
            val currentSnapshot = state.currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.move(action.position)
            
            if (nextSnapshotBeforeNotation == currentSnapshot) return state

            if (nextSnapshotBeforeNotation.board != currentSnapshot.board) {
                val boardMove = nextSnapshotBeforeNotation.lastMove!!
                val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
                val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
                
                val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
                val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))
                
                state.applyIncrement()
                    .copy(
                        snapshots = state.snapshots + nextSnapshot,
                        currentIndex = state.currentIndex + 1,
                        ui = state.ui.copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null)
                    )
            } else {
                state.copy(
                    snapshots = state.snapshots.toMutableList().apply { 
                        set(state.currentIndex, nextSnapshotBeforeNotation) 
                    },
                    ui = state.ui.copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null)
                )
            }
        }
        is GameAction.PromotionChoice -> {
            if (state.isViewingHistory) return state
            val currentSnapshot = state.currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.promote(action.move)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(action.move, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            state.applyIncrement()
                .copy(
                    snapshots = state.snapshots + nextSnapshot,
                    currentIndex = state.currentIndex + 1,
                    ui = state.ui.copy(bestMoveArrow = null)
                )
        }
        is GameAction.StepBack -> {
            if (state.canGoBack()) state.copy(currentIndex = state.currentIndex - 1, ui = state.ui.copy(bestMoveArrow = null)) else state
        }
        is GameAction.StepForward -> {
            if (state.canGoForward()) state.copy(currentIndex = state.currentIndex + 1, ui = state.ui.copy(bestMoveArrow = null)) else state
        }
        is GameAction.JumpToHistory -> {
            if (action.index in state.snapshots.indices) {
                state.copy(currentIndex = action.index, ui = state.ui.copy(bestMoveArrow = null))
            } else state
        }
        is GameAction.EngineMove -> {
            if (state.isViewingHistory) return state
            val currentSnapshot = state.currentSnapshot
            val boardMove = currentSnapshot.findMoveByUci(action.moveUci) ?: return state
            
            val nextSnapshotBeforeNotation = currentSnapshot.promote(boardMove)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            state.applyIncrement()
                .copy(
                    snapshots = state.snapshots + nextSnapshot,
                    currentIndex = state.currentIndex + 1,
                    ui = state.ui.copy(bestMoveArrow = null)
                )
        }
        is GameAction.ReverseSides -> {
            state.copy(
                whitePlayer = state.blackPlayer,
                blackPlayer = state.whitePlayer,
                ui = state.ui.copy(isBoardFlipped = !state.isBoardFlipped)
            )
        }
        is GameAction.Resign -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            val snapshot = state.currentSnapshot
            val forcedStatus = if (snapshot.activeColor == PieceColor.WHITE) GameStatus.WHITE_RESIGNED else GameStatus.BLACK_RESIGNED
            val nextSnapshot = snapshot.copy(forcedStatus = forcedStatus)
            state.copy(
                snapshots = state.snapshots.toMutableList().apply { set(state.currentIndex, nextSnapshot) }
            )
        }
        is GameAction.OfferDraw -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            val snapshot = state.currentSnapshot
            if (snapshot.drawOfferedBy != null) return state
            val nextSnapshot = snapshot.copy(drawOfferedBy = snapshot.activeColor)
            state.copy(
                snapshots = state.snapshots.toMutableList().apply { set(state.currentIndex, nextSnapshot) }
            )
        }
        is GameAction.AcceptDraw -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            val snapshot = state.currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return state
            val nextSnapshot = snapshot.copy(forcedStatus = GameStatus.DRAW)
            state.copy(
                snapshots = state.snapshots.toMutableList().apply { set(state.currentIndex, nextSnapshot) }
            )
        }
        is GameAction.DeclineDraw -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            val snapshot = state.currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return state
            val nextSnapshot = snapshot.copy(drawOfferedBy = null)
            state.copy(
                snapshots = state.snapshots.toMutableList().apply { set(state.currentIndex, nextSnapshot) }
            )
        }
        is GameAction.SetEngineThinking -> {
            state.copy(engine = state.engine.copy(isThinking = action.isThinking))
        }
        is GameAction.ChangeEngineLevel -> {
            state.copy(engine = state.engine.copy(model = action.model))
        }
        is GameAction.Tick -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            
            val activeColor = state.currentSnapshot.activeColor
            val newState = if (activeColor == PieceColor.WHITE) {
                val newTime = (state.whiteTimeMillis - action.millis).coerceAtLeast(0L)
                state.copy(whitePlayer = state.whitePlayer.copy(timeMillis = newTime))
            } else {
                val newTime = (state.blackTimeMillis - action.millis).coerceAtLeast(0L)
                state.copy(blackPlayer = state.blackPlayer.copy(timeMillis = newTime))
            }
            
            if ((activeColor == PieceColor.WHITE && newState.whiteTimeMillis == 0L) ||
                (activeColor == PieceColor.BLACK && newState.blackTimeMillis == 0L)) {
                val forcedStatus = if (activeColor == PieceColor.WHITE) GameStatus.WHITE_OUT_OF_TIME else GameStatus.BLACK_OUT_OF_TIME
                val nextSnapshot = newState.currentSnapshot.copy(forcedStatus = forcedStatus)
                newState.copy(
                    snapshots = newState.snapshots.toMutableList().apply { set(newState.currentIndex, nextSnapshot) }
                )
            } else {
                newState
            }
        }
        is GameAction.SquareLongPress -> {
            state.copy(ui = state.ui.copy(longPressedPosition = action.position))
        }
        is GameAction.ClearLongPress -> {
            state.copy(ui = state.ui.copy(longPressedPosition = null, moveEvaluations = emptyMap()))
        }
        is GameAction.SetMoveEvaluations -> {
            state.copy(ui = state.ui.copy(moveEvaluations = action.evaluations))
        }
        is GameAction.SetBestMoveArrow -> {
            val arrow = if (action.from != null && action.to != null) action.from to action.to else null
            state.copy(ui = state.ui.copy(bestMoveArrow = arrow))
        }
    }
}

private fun GameState.applyIncrement(): GameState {
    val justMovedColor = currentSnapshot.activeColor
    return if (justMovedColor == PieceColor.WHITE) {
        copy(whitePlayer = whitePlayer.copy(timeMillis = whiteTimeMillis + 3000L))
    } else {
        copy(blackPlayer = blackPlayer.copy(timeMillis = blackTimeMillis + 3000L))
    }
}
