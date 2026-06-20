package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.GameAction
import com.github.maklumi.catur.model.game.state.GameState
import com.github.maklumi.catur.model.game.state.GameStatus
import com.github.maklumi.catur.model.game.state.getNotation
import com.github.maklumi.catur.model.game.state.isInCheck

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
                
                state.copy(
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

            state.copy(
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

            state.copy(
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
    }
}
