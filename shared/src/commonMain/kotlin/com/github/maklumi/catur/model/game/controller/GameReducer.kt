package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.game.state.getNotation
import com.github.maklumi.catur.model.game.state.isInCheck
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
                val nextSnapshot = nextSnapshotBeforeNotation.copy(notation = notation)
                
                state.applyIncrement()
                    .copy(
                        snapshots = state.snapshots + nextSnapshot,
                        currentIndex = state.currentIndex + 1
                    )
            } else {
                state.copy(
                    snapshots = state.snapshots.toMutableList().apply { 
                        set(state.currentIndex, nextSnapshotBeforeNotation) 
                    }
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
            val nextSnapshot = nextSnapshotBeforeNotation.copy(notation = notation)

            state.applyIncrement()
                .copy(
                    snapshots = state.snapshots + nextSnapshot,
                    currentIndex = state.currentIndex + 1
                )
        }
        is GameAction.StepBack -> {
            if (state.canGoBack()) state.copy(currentIndex = state.currentIndex - 1) else state
        }
        is GameAction.StepForward -> {
            if (state.canGoForward()) state.copy(currentIndex = state.currentIndex + 1) else state
        }
        is GameAction.JumpToHistory -> {
            if (action.index in state.snapshots.indices) {
                state.copy(currentIndex = action.index)
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
            val nextSnapshot = nextSnapshotBeforeNotation.copy(notation = notation)

            state.applyIncrement()
                .copy(
                    snapshots = state.snapshots + nextSnapshot,
                    currentIndex = state.currentIndex + 1
                )
        }
        is GameAction.ReverseSides -> {
            state.copy(
                whitePlayer = state.blackPlayer,
                blackPlayer = state.whitePlayer,
                isBoardFlipped = !state.isBoardFlipped
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
            state.copy(isEngineThinking = action.isThinking)
        }
        is GameAction.Tick -> {
            if (state.isViewingHistory || state.currentSnapshot.status != GameStatus.ONGOING) return state
            
            val activeColor = state.currentSnapshot.activeColor
            val newState = if (activeColor == PieceColor.WHITE) {
                val newTime = (state.whiteTimeMillis - action.millis).coerceAtLeast(0L)
                state.copy(whiteTimeMillis = newTime)
            } else {
                val newTime = (state.blackTimeMillis - action.millis).coerceAtLeast(0L)
                state.copy(blackTimeMillis = newTime)
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
    }
}

private fun GameState.applyIncrement(): GameState {
    val justMovedColor = currentSnapshot.activeColor
    return if (justMovedColor == PieceColor.WHITE) {
        copy(whiteTimeMillis = whiteTimeMillis + 3000L)
    } else {
        copy(blackTimeMillis = blackTimeMillis + 3000L)
    }
}
